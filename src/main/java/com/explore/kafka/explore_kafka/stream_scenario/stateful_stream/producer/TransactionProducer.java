package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.producer;

import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class TransactionProducer {
    private static final String TOPIC = "transactions";
    private static final String[] CUSTOMERS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
    private static final String[] TYPES = {"CREDIT", "DEBIT"};
    private static final Random random = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        System.out.println("Starting transaction producer...");

        for (int i = 0; i < 10; i++) {
            String customerId = CUSTOMERS[random.nextInt(CUSTOMERS.length)];
            String transactionId = UUID.randomUUID().toString();
            double amount = 10 + (random.nextDouble() * 990); // $10 to $1000
            String type = TYPES[random.nextInt(TYPES.length)];

            Transaction txn = new Transaction(transactionId, customerId, amount, type);
            String json = mapper.writeValueAsString(txn);

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, customerId, json);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.printf("Sent: %s - %s $%.2f%n", customerId, type, amount);
                } else {
                    exception.printStackTrace();
                }
            });

            Thread.sleep(1000); // Send one transaction every 500ms
            // DUPLICATE COPY FROM PRODUCER TO TOPIC
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.printf("Sent: %s - %s $%.2f%n", customerId, type, amount);
                } else {
                    exception.printStackTrace();
                }
            });
        }

        producer.flush();
        producer.close();
        System.out.println("Producer finished");
    }
}
