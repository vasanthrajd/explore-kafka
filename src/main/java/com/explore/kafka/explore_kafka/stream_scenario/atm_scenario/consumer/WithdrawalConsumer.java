package com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class WithdrawalConsumer {
    private static final String TOPIC = "withdrawal-responses";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "withdrawal-response-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         WITHDRAWAL RESPONSE CONSUMER - ACTIVE              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> {
                    try {
                        JsonNode response = mapper.readTree(record.value());

                        String requestId = response.get("request_id").asText().substring(0, 8);
                        String customerId = response.get("customer_id").asText();
                        String atmId = response.get("atm_id").asText();
                        String status = response.get("status").asText();
                        Long balance = response.get("balance").asLong();
                        String denialReason = response.has("denial_reason") &&
                                !response.get("denial_reason").isNull()
                                ? response.get("denial_reason").asText() : "N/A";
                        int txnCount = response.get("customer_txn_count_after").asInt();
                        long processingTime = response.get("processing_time_ms").asLong();

                        System.out.println("┌─────────────────────────────────────────────────────────┐");
                        System.out.printf("│ Request:  %s...                                  │%n", requestId);
                        System.out.printf("│ Customer: %-45s │%n", customerId);
                        if ("APPROVED".equals(status)) {
                            System.out.println("│ Status:   ✓ APPROVED                                    │");
                        } else {
                            System.out.println("│ Status:   ✗ DENIED                                      │");
                            System.out.printf("│ Reason:   %-45s │%n", denialReason);
                        }

                        System.out.printf("│ Customer Balance Balance After: $%-35.2f │%n", balance);
                        System.out.printf("│ Customer Txn Count: %-33d │%n", txnCount);
                        System.out.printf("│ Processing Time: %dms%38s │%n", processingTime, "");
                        System.out.println("└─────────────────────────────────────────────────────────┘");
                        System.out.println();

                    } catch (Exception e) {
                        System.err.println("Error parsing response: " + e.getMessage());
                    }
                });
            }
        } finally {
            consumer.close();
        }
    }
}
