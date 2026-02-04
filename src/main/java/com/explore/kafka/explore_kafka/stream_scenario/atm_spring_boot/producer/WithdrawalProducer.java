package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.producer;

import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.WithdrawalRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class WithdrawalProducer {
    private static final String TOPIC = "withdrawal-requests";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        System.out.println("Sending test withdrawal requests...\n");

        // Test 1: Valid withdrawal
        sendRequest(producer, "CUST001", "ATM-NYC-001", 500);
        Thread.sleep(2000);

        // Test 2: Amount exceeds limit
        sendRequest(producer, "CUST002", "ATM-NYC-001", 2500);
        Thread.sleep(2000);

        // Test 3: Multiple requests from same customer (rate limit test)
        for (int i = 0; i < 6; i++) {
            sendRequest(producer, "CUST003", "ATM-NYC-002", 100);
            Thread.sleep(500); // 500ms apart - should hit 1-min limit on 6th
        }

        producer.flush();
        producer.close();

        System.out.println("\nAll test requests sent");
    }

    private static void sendRequest(KafkaProducer<String, String> producer,
                                    String customerId, String atmId, long amount) throws Exception {

        WithdrawalRequest request = new WithdrawalRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setCustomerId(customerId);
        request.setAtmId(atmId);
        request.setAmount(amount);
        request.setTimeStamp(System.currentTimeMillis());

        String json = mapper.writeValueAsString(request);
        producer.send(new ProducerRecord<>(TOPIC, customerId, json));

        System.out.printf("Sent: %s | Customer: %s | ATM: %s | Amount: $%d",
                request.getRequestId().substring(0, 8), customerId, atmId, amount);
        System.out.println();
    }
}
