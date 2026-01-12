package com.explore.kafka.explore_kafka.consumer.service;

import com.explore.kafka.explore_kafka.consumer.configuration.AvroSchemaLoader;
import com.explore.kafka.explore_kafka.consumer.dto.OrderEvent;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class KafkaConsumer {

    private final KafkaTemplate transactionalKafkaTemplate;

    public KafkaConsumer(KafkaTemplate transactionalKafkaTemplate) {
        this.transactionalKafkaTemplate = transactionalKafkaTemplate;
    }

    @KafkaListener(topics = "order-events", groupId = "consumer-group-101", containerFactory = "kafkaListenerContainerFactoryForPlainMessage")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        // Print statement
        System.out.println("Received new record:");
        System.out.println("Leader Epoch:" + consumerRecord.leaderEpoch().get());
        consumerRecord.headers().forEach(header -> {
            System.out.println("Header Key: " + header.key() + ", Header Value: " + new String(header.value()));
        });
        System.out.println("Partition = " + consumerRecord.partition());
        System.out.println("Offset = " + consumerRecord.offset());
        System.out.println("Key = " + consumerRecord.key());
        System.out.println("Value = " + consumerRecord.value());

    }

    @KafkaListener(topics = "order-events", groupId = "consumer-group-102", containerFactory = "kafkaListenerContainerFactoryForJson")
    public void consumeForOrderEvent(ConsumerRecord<String, OrderEvent> consumerRecordForOrderEvent) {
        // Print statement
        System.out.println("Received new record:");
        System.out.println("Leader Epoch:" + consumerRecordForOrderEvent.leaderEpoch().get());
        consumerRecordForOrderEvent.headers().forEach(header -> {
            System.out.println("Header Key: " + header.key() + ", Header Value: " + new String(header.value()));
        });
        System.out.println("Partition = " + consumerRecordForOrderEvent.partition());
        System.out.println("Offset = " + consumerRecordForOrderEvent.offset());
        System.out.println("Key = " + consumerRecordForOrderEvent.key());
        System.out.println("Value = " + consumerRecordForOrderEvent.value());
    }

    @KafkaListener(topics = "order-events-avro", groupId = "avro-consumer-group", containerFactory = "kafkaListenerContainerFactoryForAvro")
    public void consume(byte[] message) throws IOException {
        Schema schema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema.avsc");
        GenericRecord record = AvroSchemaLoader.deserializeAvro(message, schema);
        System.out.println("OrderId   : " + record.get("orderId"));
        System.out.println("Product   : " + record.get("productName"));
        System.out.println("Quantity  : " + record.get("quantity"));
//        System.out.println("CreatedAt : " + record.get("createdAt"));
    }

    @KafkaListener(topics = "order-events-avro-transaction", groupId = "transactional-avro-consumer-group",
            containerFactory = "kafkaListenerContainerFactoryForAvroTransaction")
    public void process(
            ConsumerRecord<String, byte[]> record,
            Acknowledgment ack) {
        Schema receivedSchema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema.avsc");
        Schema outputSchema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema-commit.avsc");
        transactionalKafkaTemplate.executeInTransaction(kt -> {
            // 1. Deserialize input
            GenericRecord input = null;
            try {
                input = AvroSchemaLoader.deserializeAvro(record.value(), receivedSchema);
                byte[] output = AvroSchemaLoader.serializeAvro(transform(input, outputSchema), outputSchema);                // 3. Produce output
                kt.send("output-order-events-transaction-topic", record.key(), output);
                // 4. Commit consumer offset as part of transaction
                ack.acknowledge();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    /*private GenericRecord transform(GenericRecord input) {
        if (List.of("India, USA").contains(input.get("made"))) {
            input.put("confirmation", "ACCEPTED");
        } else {
            input.put("confirmation", "IN REVIEW");
        }
        return input;
    }

    @KafkaListener(topics = "order-events-avro-transaction", groupId = "transactional-avro-consumer-group",
            containerFactory = "kafkaListenerContainerFactoryForAvroTransaction")
    public void process(
            ConsumerRecord<String, byte[]> record,
            Acknowledgment ack) {
        Schema receivedSchema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema.avsc");
        Schema outputSchema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema-commit.avsc");
        transactionalKafkaTemplate.executeInTransaction(kt -> {
            // 1. Deserialize input
            GenericRecord input = null;
            try {
                input = AvroSchemaLoader.deserializeAvro(record.value(), receivedSchema);
                // transform with output schema so the resulting record contains the `confirmation` field
                GenericRecord transformed = transform(input, outputSchema);
                byte[] output = AvroSchemaLoader.serializeAvro(transformed, outputSchema);
                // 3. Produce output
                kt.send("output-order-events-transaction-topic", record.key(), output);
                // 4. Commit consumer offset as part of transaction
                ack.acknowledge();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }*/

    private GenericRecord transform(GenericRecord input, Schema outputSchema) {
        org.apache.avro.generic.GenericData.Record out = new org.apache.avro.generic.GenericData.Record(outputSchema);
        // Copy fields that exist in the output schema from the input (if present)
        for (Schema.Field field : outputSchema.getFields()) {
            String name = field.name();
            if ("confirmation".equals(name)) {
                // skip, will set below
                continue;
            }
            Object val = input.get(name);
            out.put(name, val);
        }
        // Determine confirmation using the input's `made` value
        String made = input.get("made") == null ? null : input.get("made").toString();
        if (made != null && List.of("India", "USA").contains(made)) {
            out.put("confirmation", "ACCEPTED");
        } else {
            out.put("confirmation", "IN REVIEW");
        }
        return out;
    }


}
