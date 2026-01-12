package com.explore.kafka.explore_kafka.producer.configuration;


import com.explore.kafka.explore_kafka.consumer.dto.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private final KafkaConsumerProperties kafkaConsumerProperties;

    public KafkaConsumerConfig(KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
    }

    private Map<String, Object> commonKafkaProperties() {
        String keyDeserializer = kafkaConsumerProperties.getKeyDeserializer();
        String valueDeserializer = kafkaConsumerProperties.getValueDeserializer();

        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaConsumerProperties.getBootstrapServers());
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                kafkaConsumerProperties.getGroupId());
        props.put(
                ConsumerConfig.CLIENT_ID_CONFIG,
                kafkaConsumerProperties.getClientId());

        // Configure ErrorHandlingDeserializer as the outer deserializer class and
        // provide the delegate (actual) deserializer class via the ErrorHandlingDeserializer keys.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        try {
            // delegate classes - the classes that actually perform deserialization
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, Class.forName(keyDeserializer));
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, Class.forName(valueDeserializer));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Configured deserializer class not found", e);
        }
        return props;
    }
    public ConsumerFactory<String, byte[]> consumerFactoryForAvroTransaction() {
        Map<String, Object> props = commonKafkaProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // Create a JacksonJsonDeserializer for the target type and trust the package.
        ByteArrayDeserializer delegate = new ByteArrayDeserializer();


        // Wrap it with ErrorHandlingDeserializer so any SerializationException is handled
        // by the container's error handler instead of bubbling up.
        ErrorHandlingDeserializer<byte[]> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(delegate);

        // Provide the key and value deserializer instances to the factory.
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactoryForAvroTransaction(DefaultErrorHandler commonErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryForAvroTransaction());
        factory.setCommonErrorHandler(commonErrorHandler);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    /**
     * Common error handler for all Kafka listeners. This DefaultErrorHandler will retry
     * a few times (FixedBackOff) and then invoke the recoverer which logs the failure.
     * You can replace the recoverer with a DeadLetterPublishingRecoverer if you want
     * to publish failed records to a DLQ topic.
     */
    @Bean
    public DefaultErrorHandler commonErrorHandler() {
        // retry up to 3 times with 1 second interval
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // final recoverer logic after retries exhausted
            logger.error("Failed to process record with key={} topic-partition={} offset={}",
                    record.key(),
                    record.topic() + "-" + record.partition(),
                    record.offset(),
                    exception);
            // You can add extra actions here: publish to DLQ, alerting, metrics, etc.
        }, backOff);

        // Optionally configure which exceptions are not retried (e.g., for poison messages).
        // errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        return errorHandler;
    }


}
