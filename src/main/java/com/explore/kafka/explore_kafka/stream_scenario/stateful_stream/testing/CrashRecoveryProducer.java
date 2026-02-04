package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.testing;

import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

public class CrashRecoveryProducer {
    private static final String TOPIC = "transactions";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        crashTestForSingleCustomer(producer);
        //crashTestForMultipleCustomer(producer);
    }

    private static void crashTestForMultipleCustomer(KafkaProducer<String, String> producer) throws InterruptedException, JsonProcessingException {
        final String[] CUSTOMERS = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
        final Random random = new Random();
        final String[] TYPES = {"CREDIT", "DEBIT"};System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         EXACTLY-ONCE CRASH RECOVERY TEST WITH SINGLE CUSTOMER║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("TEST INSTRUCTIONS:");
        System.out.println("1. This will send 10 transactions of $100 each with Transaction Type chosen at random");
        System.out.println("2. Expected final balance: $1,000.00");
        System.out.println("3. AFTER you see 'Transaction 5/10 sent':");
        System.out.println("   → Go to Streams app terminal");
        System.out.println("   → Press Ctrl+C to KILL the app");
        System.out.println("   → Wait 3 seconds");
        System.out.println("   → RESTART the Streams app");
        System.out.println("4. Let all 10 transactions complete");
        System.out.println("5. Check final balance in consumer");
        System.out.println();
        System.out.println("EXPECTED RESULT WITH EOS:");
        System.out.println("  ✓ Final balance = $1,000.00 (10 transactions)");
        System.out.println("  ✓ Transaction count = 10");
        System.out.println();
        System.out.println("EXPECTED RESULT WITHOUT EOS:");
        System.out.println("  ✗ Final balance > $1,000.00 (transactions 1-5 processed twice)");
        System.out.println();
        System.out.println("Starting in 3 seconds...");
        Thread.sleep(3000);
        System.out.println();
        Map<String, List<Transaction>> transactionMap = new HashMap<>();
        for (int i = 1; i <= 10; i++) {

            String customerId = CUSTOMERS[random.nextInt(CUSTOMERS.length)];
            String transactionId = UUID.randomUUID().toString();
            String type = TYPES[random.nextInt(TYPES.length)];
            Transaction txn = new Transaction(
                    transactionId,
                    customerId,
                    100.0,
                    type
            );

            String json = mapper.writeValueAsString(txn);
            producer.send(new ProducerRecord<>(TOPIC, customerId, json));
            List<Transaction> list;
            if (transactionMap.containsKey(txn.getCustomerId())) {
                list = transactionMap.get(txn.getCustomerId());
            } else {
                list = new ArrayList<>();
            }
            list.add(txn);
            transactionMap.put(txn.getCustomerId(), list);

            System.out.printf("► Transaction %d/10 sent | for Customer ID: %s | TXN-ID: %s | Amount: $100.00 | of type %s transaction \n",
                    i, txn.getCustomerId(), txn.getTransactionId(), txn.getTransactionType());


            if (i == 5) {
                System.out.println();
                System.out.println("⚠️  NOW IS THE TIME: KILL THE STREAMS APP (Ctrl+C)");
                System.out.println("    Then wait 3 seconds and RESTART it");
                System.out.println("    Continuing in 8 seconds...");
                System.out.println();
                Thread.sleep(8000); // Give time to kill and restart
            } else {
                Thread.sleep(2000);
            }
        }
        transactionMap.forEach((custId, transactions) -> {
            System.out.println("For Customer ID " + custId);
            transactions.forEach(System.out::println);
        });

        producer.flush();
        producer.close();

        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("ALL 10 TRANSACTIONS SENT");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("CHECK CONSUMER OUTPUT:");
        System.out.println("  Expected Count:   10 transactions");
        System.out.println();
    }

    private static void crashTestForSingleCustomer(KafkaProducer<String, String> producer) throws InterruptedException, JsonProcessingException {
        String customerId = "CRASH_TEST_CUSTOMER";

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║ EXACTLY-ONCE CRASH RECOVERY TEST WITH SINGLE CUSTOMER      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("TEST INSTRUCTIONS:");
        System.out.println("1. This will send 10 transactions of $100 each");
        System.out.println("2. Expected final balance: $1,000.00");
        System.out.println("3. AFTER you see 'Transaction 5/10 sent':");
        System.out.println("   → Go to Streams app terminal");
        System.out.println("   → Press Ctrl+C to KILL the app");
        System.out.println("   → Wait 3 seconds");
        System.out.println("   → RESTART the Streams app");
        System.out.println("4. Let all 10 transactions complete");
        System.out.println("5. Check final balance in consumer");
        System.out.println();
        System.out.println("EXPECTED RESULT WITH EOS:");
        System.out.println("  ✓ Final balance = $1,000.00 (10 transactions)");
        System.out.println("  ✓ Transaction count = 10");
        System.out.println();
        System.out.println("EXPECTED RESULT WITHOUT EOS:");
        System.out.println("  ✗ Final balance > $1,000.00 (transactions 1-5 processed twice)");
        System.out.println();
        System.out.println("Starting in 3 seconds...");
        Thread.sleep(3000);
        System.out.println();
        for (int i = 1; i <= 10; i++) {
            Transaction txn = new Transaction(
                    "TXN-" + String.format("%03d", i),
                    customerId,
                    100.0,
                    "CREDIT"
            );

            String json = mapper.writeValueAsString(txn);
            producer.send(new ProducerRecord<>(TOPIC, customerId, json));

            System.out.printf("► Transaction %d/10 sent | TXN-ID: %s | Amount: $100.00 | Expected Balance: $%.2f%n",
                    i, txn.getTransactionId(), i * 100.0);

            if (i == 5) {
                System.out.println();
                System.out.println("⚠️  NOW IS THE TIME: KILL THE STREAMS APP (Ctrl+C)");
                System.out.println("    Then wait 3 seconds and RESTART it");
                System.out.println("    Continuing in 8 seconds...");
                System.out.println();
                Thread.sleep(8000); // Give time to kill and restart
            } else {
                Thread.sleep(2000);
            }
        }

        producer.flush();
        producer.close();

        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("ALL 10 TRANSACTIONS SENT");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("CHECK CONSUMER OUTPUT:");
        System.out.println("  Expected Balance: $1,000.00");
        System.out.println("  Expected Count:   10 transactions");
        System.out.println();
    }
}
