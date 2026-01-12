package com.explore.kafka.explore_kafka.producer.service;

import com.explore.kafka.explore_kafka.consumer.configuration.AvroSchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "output-order-events-transaction-topic", groupId = "avro-consumer-group", containerFactory = "kafkaListenerContainerFactoryForAvroTransaction")
    public void consume(ConsumerRecord<String, byte[]> consumerRecord,
                        Acknowledgment ack) throws IOException {
        Schema schema =
                AvroSchemaLoader.loadSchema("avro/order-event-schema-commit.avsc");
        GenericRecord record = AvroSchemaLoader.deserializeAvro(consumerRecord.value(), schema);
        System.out.println("OrderId   : " + record.get("orderId"));
        System.out.println("Product   : " + record.get("productName"));
        System.out.println("Quantity  : " + record.get("quantity"));
        System.out.println("Quantity  : " + record.get("confirmation"));
        ack.acknowledge();
//        System.out.println("CreatedAt : " + record.get("createdAt"));
    }
}

