package com.explore.kafka.explore_kafka.consumer.configuration;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
public class ProducerConfig {

    private final KafkaProducerProperties kafkaProducerProperties;

    public ProducerConfig(KafkaProducerProperties kafkaProducerProperties) {
        this.kafkaProducerProperties = kafkaProducerProperties;
    }

    private Map<String, Object> commonProducerConfigProps() {
        Map<String, Object> props = new HashMap<>();

        // Ensure bootstrap servers is a comma separated string
        List<String> servers = kafkaProducerProperties.getBootstrapServers();
        if (servers != null && !servers.isEmpty()) {
            props.put(BOOTSTRAP_SERVERS_CONFIG, String.join(",", servers));
        }

        // Default to StringSerializer if not configured
        Class<?> keySerializerClass = StringSerializer.class;
        KafkaProducerProperties.Producer producerProps = kafkaProducerProperties.getProducer();
        if (producerProps != null) {
            String keySerializer = producerProps.getKeySerializer();
            String valueSerializer = producerProps.getValueSerializer();
            try {
                if (keySerializer != null && !keySerializer.isEmpty()) {
                    keySerializerClass = Class.forName(keySerializer);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Configured serializer class not found", e);
            }

            props.put(KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);

            // Reliability settings - put only when present
            if (producerProps.getAcks() != null) {
                props.put(ACKS_CONFIG, producerProps.getAcks());
            }
            if (producerProps.getEnableIdempotence() != null) {
                props.put(ENABLE_IDEMPOTENCE_CONFIG, producerProps.getEnableIdempotence());
            }
            if (producerProps.getRetries() != null) {
                props.put(RETRIES_CONFIG, producerProps.getRetries());
            }
        } else {
            // If no producer nested properties, still set default serializers
            props.put(KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        }
        return props;
    }

    @Bean
    public ProducerFactory<String, byte[]> producerFactoryWithAvroForTransaction() {
        Map<String, Object> props = commonProducerConfigProps();
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, OrderEventPartitioner.class.getName());
        props.put(TRANSACTIONAL_ID_CONFIG, "cus-tx-1");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, byte[]> transactionalKafkaTemplate() {
        KafkaTemplate<String, byte[]> kafkaTemplate = new KafkaTemplate<>(producerFactoryWithAvroForTransaction());
        kafkaTemplate.setTransactionIdPrefix("consumer-producer-");
        return kafkaTemplate;
    }

}
