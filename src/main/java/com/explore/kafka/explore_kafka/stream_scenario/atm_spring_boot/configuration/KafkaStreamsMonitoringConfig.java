package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.configuration;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

@Configuration
@Slf4j
public class KafkaStreamsMonitoringConfig {

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsCustomizer() {
        return factoryBean -> {
            // Log state transitions
            factoryBean.setStateListener((newState, oldState) ->
                log.info("KafkaStreams state changed from {} to {}", oldState, newState)
            );

            // Install an uncaught exception handler to capture root causes
            factoryBean.setStreamsUncaughtExceptionHandler((Throwable throwable) -> {
                log.error("Uncaught exception in Kafka Streams topology", throwable);
                // Decide behaviour: SHUTDOWN_CLIENT is safe; REPLACE_THREAD tries auto-recovery
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
            });
        };
    }
}