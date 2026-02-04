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

public class UserProfileGenerator {

    private final KafkaProducer<String, GenericRecord> producer;

    public UserProfileGenerator(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);

        this.producer = new KafkaProducer<>(props);
    }

    public void createUserProfile(String userId, double avgAmount,
                                  String homeLocation, int riskScore) {
        String schemaString = """
            {
              "type": "record",
              "name": "UserProfile",
              "namespace": "com.enterprise.fraud",
              "fields": [
                {"name": "user_id", "type": "string"},
                {"name": "avg_transaction_amount", "type": {"type": "bytes", "logicalType": "decimal", "precision": 10, "scale": 2}},
                {"name": "home_location", "type": "string"},
                {"name": "account_creation_date", "type": "long", "logicalType": "timestamp-millis"},
                {"name": "risk_score", "type": "int"}
              ]
            }
            """;

        Schema schema = new Schema.Parser().parse(schemaString);
        GenericRecord record = new GenericData.Record(schema);

        record.put("user_id", userId);
        record.put("avg_transaction_amount", decimalToBytes(avgAmount));
        record.put("home_location", homeLocation);
        record.put("account_creation_date", Instant.now().toEpochMilli());
        record.put("risk_score", riskScore);

        producer.send(new ProducerRecord<>("user-profiles", userId, record));
        System.out.println("Created user profile: " + userId);
    }

    private ByteBuffer decimalToBytes(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ByteBuffer.wrap(decimal.unscaledValue().toByteArray());
    }

    public void close() {
        producer.close();
    }

    public static void main(String[] args) {
        UserProfileGenerator generator = new UserProfileGenerator(
                "localhost:9092",
                "http://localhost:8081"
        );

        try {
            // Create test user profiles
            generator.createUserProfile("user_001", 100.0, "New York", 3);
            generator.createUserProfile("user_002", 150.0, "Chicago", 2);
            generator.createUserProfile("user_003", 200.0, "Los Angeles", 4);
            generator.createUserProfile("user_004", 75.0, "Houston", 5);
            generator.createUserProfile("user_005", 300.0, "Phoenix", 1);

            System.out.println("All user profiles created successfully");
        } finally {
            generator.close();
        }
    }
}
