package com.explore.kafka.explore_kafka.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class KafkaStreamsRouterApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsRouterApplication.class, args);
    }
}
