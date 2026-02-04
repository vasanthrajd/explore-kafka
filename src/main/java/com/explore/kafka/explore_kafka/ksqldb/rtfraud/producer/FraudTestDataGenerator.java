package com.explore.kafka.explore_kafka.ksqldb.rtfraud.producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class FraudTestDataGenerator {
    private static final String[] LOCATIONS = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
            "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"
    };

    private static final String[] MERCHANTS = {
            "Amazon", "Walmart", "Target", "Best Buy", "Apple Store",
            "Costco", "Home Depot", "CVS", "Walgreens", "Starbucks"
    };

    private final KafkaProducer<String, GenericRecord> producer;
    private final Random random = new Random();

    public FraudTestDataGenerator(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);

        this.producer = new KafkaProducer<>(props);
    }

    /**
     * Generate legitimate transactions
     */
    public void generateLegitimateTransactions(int count, String userId,
                                               double avgAmount, String homeLocation) {
        for (int i = 0; i < count; i++) {
            GenericRecord transaction = createTransaction(
                    UUID.randomUUID().toString(),
                    userId,
                    generateNormalAmount(avgAmount, avgAmount * 0.3),
                    MERCHANTS[random.nextInt(MERCHANTS.length)],
                    homeLocation,
                    Instant.now().toEpochMilli(),
                    generateCardLastFour()
            );

            producer.send(new ProducerRecord<>("transactions",
                    userId,
                    transaction));

            try {
                Thread.sleep(100); // 10 TPS per user
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Generate high-value fraud scenario
     */
    public void generateHighValueFraud(String userId, String homeLocation) {
        GenericRecord transaction = createTransaction(
                UUID.randomUUID().toString(),
                userId,
                15000.00 + random.nextDouble() * 10000, // $15k-$25k
                MERCHANTS[random.nextInt(MERCHANTS.length)],
                homeLocation,
                Instant.now().toEpochMilli(),
                generateCardLastFour()
        );

        producer.send(new ProducerRecord<>("transactions", userId, transaction));
        System.out.println("Generated HIGH_VALUE fraud for user: " + userId);
    }

    /**
     * Generate velocity fraud scenario (burst of transactions)
     */
    public void generateVelocityFraud(String userId, String homeLocation,
                                      double avgAmount) {
        System.out.println("Generating VELOCITY fraud for user: " + userId);

        for (int i = 0; i < 15; i++) { // 15 transactions in quick succession
            GenericRecord transaction = createTransaction(
                    UUID.randomUUID().toString(),
                    userId,
                    avgAmount + random.nextDouble() * avgAmount,
                    MERCHANTS[random.nextInt(MERCHANTS.length)],
                    homeLocation,
                    Instant.now().toEpochMilli(),
                    generateCardLastFour()
            );

            producer.send(new ProducerRecord<>("transactions", userId, transaction));

            try {
                Thread.sleep(10); // Very fast succession
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Generate location anomaly scenario
     */
    public void generateLocationAnomaly(String userId, double avgAmount,
                                        String homeLocation) {
        String foreignLocation = LOCATIONS[random.nextInt(LOCATIONS.length)];
        while (foreignLocation.equals(homeLocation)) {
            foreignLocation = LOCATIONS[random.nextInt(LOCATIONS.length)];
        }

        GenericRecord transaction = createTransaction(
                UUID.randomUUID().toString(),
                userId,
                avgAmount * (5 + random.nextDouble() * 3), // 5-8x normal
                MERCHANTS[random.nextInt(MERCHANTS.length)],
                foreignLocation,
                Instant.now().toEpochMilli(),
                generateCardLastFour()
        );

        producer.send(new ProducerRecord<>("transactions", userId, transaction));
        System.out.println("Generated LOCATION_ANOMALY fraud for user: " + userId +
                " in " + foreignLocation);
    }

    /**
     * Generate amount anomaly scenario
     */
    public void generateAmountAnomaly(String userId, double avgAmount,
                                      String homeLocation) {
        GenericRecord transaction = createTransaction(
                UUID.randomUUID().toString(),
                userId,
                avgAmount * (6 + random.nextDouble() * 4), // 6-10x normal
                MERCHANTS[random.nextInt(MERCHANTS.length)],
                homeLocation,
                Instant.now().toEpochMilli(),
                generateCardLastFour()
        );

        producer.send(new ProducerRecord<>("transactions", userId, transaction));
        System.out.println("Generated AMOUNT_ANOMALY fraud for user: " + userId);
    }

    private GenericRecord createTransaction(String transactionId, String userId,
                                            double amount, String merchantId,
                                            String location, long timestamp,
                                            String cardLastFour) {
        String schemaString = """
            {
              "type": "record",
              "name": "Transaction",
              "namespace": "com.enterprise.fraud",
              "fields": [
                {"name": "transaction_id", "type": "string"},
                {"name": "user_id", "type": "string"},
                {"name": "amount", "type": {"type": "bytes", "logicalType": "decimal", "precision": 10, "scale": 2}},
                {"name": "merchant_id", "type": "string"},
                {"name": "location", "type": "string"},
                {"name": "transaction_time", "type": "long", "logicalType": "timestamp-millis"},
                {"name": "card_last_four", "type": "string"}
              ]
            }
            """;

        Schema schema = new Schema.Parser().parse(schemaString);
        GenericRecord record = new GenericData.Record(schema);

        record.put("transaction_id", transactionId);
        record.put("user_id", userId);
        record.put("amount", decimalToBytes(amount));
        record.put("merchant_id", merchantId);
        record.put("location", location);
        record.put("transaction_time", timestamp);
        record.put("card_last_four", cardLastFour);

        return record;
    }

    private ByteBuffer decimalToBytes(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ByteBuffer.wrap(decimal.unscaledValue().toByteArray());
    }

    private double generateNormalAmount(double mean, double stdDev) {
        return Math.abs(random.nextGaussian() * stdDev + mean);
    }

    private String generateCardLastFour() {
        return String.format("%04d", random.nextInt(10000));
    }

    public void close() {
        producer.close();
    }

    public static void main(String[] args) {
        FraudTestDataGenerator generator = new FraudTestDataGenerator(
                "localhost:9092",
                "http://localhost:8081"
        );

        try {
            // Generate test scenarios
            String userId1 = "user_001";
            String userId2 = "user_002";
            String userId3 = "user_003";

            // Scenario 1: Normal transactions
            System.out.println("Generating normal transactions...");
            generator.generateLegitimateTransactions(10, userId1, 100.0, "New York");

            Thread.sleep(2000);

            // Scenario 2: High-value fraud
            System.out.println("\nGenerating high-value fraud...");
            generator.generateHighValueFraud(userId1, "New York");

            Thread.sleep(2000);

            // Scenario 3: Velocity fraud
            System.out.println("\nGenerating velocity fraud...");
            generator.generateVelocityFraud(userId2, "Chicago", 150.0);

            Thread.sleep(2000);

            // Scenario 4: Location anomaly
            System.out.println("\nGenerating location anomaly...");
            generator.generateLocationAnomaly(userId3, 200.0, "Los Angeles");

            Thread.sleep(2000);

            // Scenario 5: Amount anomaly
            System.out.println("\nGenerating amount anomaly...");
            generator.generateAmountAnomaly(userId1, 100.0, "New York");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            generator.close();
        }
    }
}
