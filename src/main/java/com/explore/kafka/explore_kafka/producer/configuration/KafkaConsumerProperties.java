package com.explore.kafka.explore_kafka.producer.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "spring.kafka.consumer")
@Component
@Getter
@Setter
public class KafkaConsumerProperties {

    private List<String> bootstrapServers;

    private String groupId;

    private String keyDeserializer;

    private String valueDeserializer;

    private String clientId;
}
