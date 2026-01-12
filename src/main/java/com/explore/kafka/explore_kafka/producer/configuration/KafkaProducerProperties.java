package com.explore.kafka.explore_kafka.producer.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds properties under prefix "spring.kafka" so you can inject this bean and
 * read bootstrap servers and producer-specific properties as instance variables.
 */
@ConfigurationProperties(prefix = "spring.kafka")
@Component
@Getter
@Setter
public class KafkaProducerProperties {
    /**
     * spring.kafka.bootstrap-servers (can be a comma-separated list)
     */
    private List<String> bootstrapServers;

    // Explicit getter/setter in addition to Lombok to ensure methods exist even
    // when annotation processing is not available in the build/IDE.
    public List<String> getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(List<String> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Nested "producer" properties (spring.kafka.producer.*)
     */
    private Producer producer = new Producer();

    public Producer getProducer() {
        return this.producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Getter
    @Setter
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
        private String acks;
        private Integer retries;
        private Boolean enableIdempotence;

        // Explicit getters/setters in addition to Lombok
        public String getKeySerializer() {
            return keySerializer;
        }

        public void setKeySerializer(String keySerializer) {
            this.keySerializer = keySerializer;
        }

        public String getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(String valueSerializer) {
            this.valueSerializer = valueSerializer;
        }

        public String getAcks() {
            return acks;
        }

        public void setAcks(String acks) {
            this.acks = acks;
        }

        public Integer getRetries() {
            return retries;
        }

        public void setRetries(Integer retries) {
            this.retries = retries;
        }

        public Boolean getEnableIdempotence() {
            return enableIdempotence;
        }

        public void setEnableIdempotence(Boolean enableIdempotence) {
            this.enableIdempotence = enableIdempotence;
        }
    }
}
