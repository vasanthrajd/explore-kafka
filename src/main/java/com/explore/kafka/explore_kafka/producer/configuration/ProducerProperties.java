package com.explore.kafka.explore_kafka.producer.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "producer")
@Component
@Getter
@Setter
public class ProducerProperties {
    private String name;
    private String topic;
    private int retryCount;
    private boolean enabled;
}
