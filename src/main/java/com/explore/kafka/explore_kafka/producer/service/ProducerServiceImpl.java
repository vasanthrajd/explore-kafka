package com.explore.kafka.explore_kafka.producer.service;

import com.explore.kafka.explore_kafka.producer.dto.OrderEvent;
import com.explore.kafka.explore_kafka.producer.configuration.AvroSchemaLoader;
import com.explore.kafka.explore_kafka.producer.configuration.ProducerProperties;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class ProducerServiceImpl implements ProducerService{

    private final KafkaTemplate kafkaTemplate;

    private final KafkaTemplate jsonKafkaTemplate;

    private final KafkaTemplate avroKafkaTemplate;

    private final KafkaTemplate transactionalKafkaTemplate;

    private final ProducerProperties producerProperties;

    public ProducerServiceImpl(KafkaTemplate kafkaTemplate, KafkaTemplate jsonKafkaTemplate, KafkaTemplate avroKafkaTemplate, KafkaTemplate transactionalKafkaTemplate, ProducerProperties producerProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonKafkaTemplate = jsonKafkaTemplate;
        this.avroKafkaTemplate = avroKafkaTemplate;
        this.transactionalKafkaTemplate = transactionalKafkaTemplate;
        this.producerProperties = producerProperties;
    }

    @Override
    public CompletableFuture<Long> publishMessageToKafkaTopic(String key, String value, Integer partition) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(producerProperties.getTopic(), partition, key, value);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(producerRecord);
        return future.handle((sendResult, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + throwable.getMessage());
                throw new CompletionException(throwable);
            }
            return sendResult.getRecordMetadata().offset();
        });
        /*return future.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + throwable.getMessage());
            }
        }).thenApply(sendResult -> sendResult.getRecordMetadata().offset());*/

        /*return kafkaTemplate.send(producerRecord)
                .thenApply(result -> ((RecordMetadata)result).offset())
                .exceptionally(ex -> {
                    log.error("Kafka publish failed for key={}", key, ex);
                    throw new RuntimeException(String.valueOf(ex));
                });*/
    }

    @Override
    public CompletableFuture<Long> pushOrderEventToKafkaTopic(OrderEvent orderEvent) {
        ProducerRecord<String, OrderEvent> producerRecord = new ProducerRecord<>(producerProperties.getTopic(), orderEvent);
        CompletableFuture<SendResult<String, String>> future = jsonKafkaTemplate.send(producerRecord);
        return future.handle((sendResult, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + throwable.getMessage());
                throw new CompletionException(throwable);
            }
            return sendResult.getRecordMetadata().offset();
        });
    }

    @Override
    public CompletableFuture<Long> pushOrderEventToKafkaTopicUsingAvro(OrderEvent orderEvent) throws IOException {
        Schema schema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema.avsc");
        GenericRecord record = new GenericData.Record(schema);
        record.put("orderId", orderEvent.orderId());
        record.put("productName", orderEvent.productName());
        record.put("quantity", orderEvent.quantity());
        record.put("made", orderEvent.made());
        byte[] avroBytes = AvroSchemaLoader.serializeAvro(record, schema);
        ProducerRecord<String, byte[]> producerRecord =
                new ProducerRecord<>("order-events-avro", avroBytes);
        CompletableFuture<SendResult<String, String>> future = avroKafkaTemplate.send(producerRecord);
        return future.handle((sendResult, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + throwable.getMessage());
                throw new CompletionException(throwable);
            }
            return sendResult.getRecordMetadata().offset();
        });
    }

    @Override
    @Transactional("kafkaTransactionManager")
    public CompletableFuture<Long> pushOrderEventToKafkaTopicUsingAvroTransaction(OrderEvent orderEvent) throws IOException {
        Schema schema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema.avsc");
        GenericRecord record = new GenericData.Record(schema);
        record.put("orderId", orderEvent.orderId());
        record.put("productName", orderEvent.productName());
        record.put("quantity", orderEvent.quantity());
        record.put("made", orderEvent.made());
        byte[] avroBytes = AvroSchemaLoader.serializeAvro(record, schema);
        ProducerRecord<String, byte[]> producerRecord =
                new ProducerRecord<>("order-events-avro-transaction", avroBytes);
        CompletableFuture<RecordMetadata> future =
                (CompletableFuture<RecordMetadata>) transactionalKafkaTemplate.executeInTransaction(kt ->
                        kt.send(producerRecord)
                                .thenApply(sendResult -> ((SendResult)sendResult).getRecordMetadata())
                );
        return future.handle((sendResult, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + throwable.getMessage());
                throw new CompletionException(throwable);
            }
            return sendResult.offset();
        });

    }

}
