package com.explore.flink.explore_flink.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Instant;
import java.util.*;

public class OrderGenerator {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String[] CUSTOMER_IDS = {"C001", "C002", "C003", "C004", "C005"};
    private static final String[] PRODUCT_IDS = {"P001", "P002", "P003", "P004", "P005"};
    private static final String[] PAYMENT_METHODS = {"CREDIT_CARD", "DEBIT_CARD", "PAYPAL"};

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            Random random = new Random();

            System.out.println("Generating normal order traffic...");

            // Generate 100 normal orders
            for (int i = 0; i < 100; i++) {
                String orderId = "ORD-" + UUID.randomUUID().toString();
                String customerId = CUSTOMER_IDS[random.nextInt(CUSTOMER_IDS.length)];

                String orderString = buildOrderRecordAsString(orderId, customerId, PRODUCT_IDS[random.nextInt(PRODUCT_IDS.length)],
                        50 + random.nextDouble() * 450,
                        PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)]);
                ProducerRecord<String, String> record =
                        new ProducerRecord<>("orders", orderId, orderString);
                producer.send(record);

                if ((i + 1) % 10 == 0) {
                    System.out.println("Sent " + (i + 1) + " orders");
                }
                Thread.sleep(100);
            }

            System.out.println("\nInjecting fraud pattern (high velocity)...");

            // Inject fraud: 6 orders from same customer in 2 minutes
            String fraudCustomer = "C003"; // This customer has risk_score=75
            for (int i = 0; i < 6; i++) {
                String orderId = "FRAUD-" + UUID.randomUUID().toString();

                var postOrder = buildOrderRecordAsString(orderId, fraudCustomer, PRODUCT_IDS[random.nextInt(PRODUCT_IDS.length)],
                        100 + random.nextDouble() * 900, "Different Address " + i, "10.0.0." + i);
                ProducerRecord<String, String> record =
                        new ProducerRecord<>("orders", orderId, postOrder);
                producer.send(record);

                System.out.println("Sent fraud order " + (i + 1) + "/6");
                Thread.sleep(20000); // 20 seconds between orders
            }

            System.out.println("\nInjecting large transaction...");

            // Large transaction from normal customer

            String largeOrderId = "LARGE-" + UUID.randomUUID().toString();
            var largeOrder = buildOrderRecordAsString(largeOrderId, "C001", "P001",
                    5500.00, "123 Main St", "192.168.1.1");
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("orders", largeOrderId, largeOrder);
            producer.send(record);

            System.out.println("Data generation complete!");
        }
    }

    private static String buildOrderRecordAsString(String orderId, String fraudCustomer, String productId,
                                                   double amount,
                                                   String shippingAddress,
                                                   String ipAddress) throws JsonProcessingException {
        Map<String, Object> order = new HashMap<>();

        order.put("order_id", orderId);
        order.put("customer_id", fraudCustomer);
        order.put("product_id", productId);
        order.put("amount", amount);
        order.put("payment_method", "CREDIT_CARD");
        order.put("shipping_address", shippingAddress);
        order.put("ip_address", ipAddress);
        order.put("user_agent", "Mozilla/5.0");
        order.put("event_time", Instant.now().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(order);
    }

    private static String buildOrderRecordAsString(String orderId, String customerId, String productId, Double amount,
                                                   String paymentMethod) throws JsonProcessingException {
        Random random = new Random();
        Map<String, Object> order = new HashMap<>();

        order.put("order_id", orderId);
        order.put("customer_id", customerId);
        order.put("product_id", productId);
        order.put("amount", amount);
        order.put("payment_method", paymentMethod);
        order.put("shipping_address", "123 Main St, City, Country");
        order.put("ip_address", "192.168." + random.nextInt(256) + "." + random.nextInt(256));
        order.put("user_agent", "Mozilla/5.0");
        order.put("event_time", Instant.now().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(order);
    }

}