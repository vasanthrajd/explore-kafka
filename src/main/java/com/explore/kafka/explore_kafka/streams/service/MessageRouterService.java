package com.explore.kafka.explore_kafka.streams.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
@Service
@Slf4j
*/
public class MessageRouterService {

/*
    @Value("${kafka.topics.input}")
    private String inputTopic;

    @Value("${kafka.topics.order}")
    private String orderTopic;

    @Value("${kafka.topics.payment}")
    private String paymentTopic;

    @Value("${kafka.topics.notification}")
    private String notificationTopic;

    @Value("${kafka.topics.error}")
    private String errorTopic;

    @Value("${kafka.topics.default}")
    private String defaultTopic;

    private final ObjectMapper objectMapper;

    public MessageRouterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Building Kafka Streams routing topology...");

        // Create stream from input topic
        KStream<String, String> inputStream = streamsBuilder.stream(
                inputTopic,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Log incoming messages
        inputStream.peek((key, value) ->
                log.info("Received message - Key: {}, Value: {}", key, value));

        KStream<String, String>[] branches = (KStream<String, String>[]) new KStream[5];
        branches[0] = inputStream.filter(isOrderMessage());
        branches[1] = inputStream.filter(isPaymentMessage());
        branches[2] = inputStream.filter(isNotificationMessage());
// Error branch: messages that fail JSON parsing
        branches[3] = inputStream.filter((key, value) -> {
            try {
                objectMapper.readTree(value);
                return false;
            } catch (Exception e) {
                return true;
            }
        });
// Default branch: valid JSON but unknown type
        branches[4] = inputStream.filter((key, value) -> {
            try {
                JsonNode node = objectMapper.readTree(value);
                String messageType = node.path("type").asText("");
                return !( "ORDER".equalsIgnoreCase(messageType)
                        || "PAYMENT".equalsIgnoreCase(messageType)
                        || "NOTIFICATION".equalsIgnoreCase(messageType) );
            } catch (Exception e) {
                return false;
            }
        });
        // Branch streams based on message type
        */
/*KStream<String, String>[] branches = inputStream.split().branch(
                isOrderMessage(),
                isPaymentMessage(),
                isNotificationMessage(),
                (key, value) -> true  // Default catch-all
        );
*//*

        // Route to ORDER topic
        branches[0]
                .peek((key, value) -> log.info("Routing to ORDER topic - Key: {}", key))
                .to(orderTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Route to PAYMENT topic
        branches[1]
                .peek((key, value) -> log.info("Routing to PAYMENT topic - Key: {}", key))
                .to(paymentTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Route to NOTIFICATION topic
        branches[2]
                .peek((key, value) -> log.info("Routing to NOTIFICATION topic - Key: {}", key))
                .to(notificationTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Route to ERROR topic
        branches[3]
                .peek((key, value) -> log.warn("Routing to ERROR topic - Key: {}", key))
                .to(errorTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Route to DEFAULT topic
        branches[4]
                .peek((key, value) -> log.info("Routing to DEFAULT topic - Key: {}", key))
                .to(defaultTopic, Produced.with(Serdes.String(), Serdes.String()));

        log.info("Kafka Streams routing topology built successfully");
    }

    private Predicate<String, String> isOrderMessage() {
        return (key, value) -> {
            try {
                JsonNode node = objectMapper.readTree(value);
                String messageType = node.path("type").asText("");
                return "ORDER".equalsIgnoreCase(messageType);
            } catch (Exception e) {
                log.error("Error parsing ORDER message: {}", e.getMessage());
                return false;
            }
        };
    }

    private Predicate<String, String> isPaymentMessage() {
        return (key, value) -> {
            try {
                JsonNode node = objectMapper.readTree(value);
                String messageType = node.path("type").asText("");
                return "PAYMENT".equalsIgnoreCase(messageType);
            } catch (Exception e) {
                log.error("Error parsing PAYMENT message: {}", e.getMessage());
                return false;
            }
        };
    }

    private Predicate<String, String> isNotificationMessage() {
        return (key, value) -> {
            try {
                JsonNode node = objectMapper.readTree(value);
                String messageType = node.path("type").asText("");
                return "NOTIFICATION".equalsIgnoreCase(messageType);
            } catch (Exception e) {
                log.error("Error parsing NOTIFICATION message: {}", e.getMessage());
                return false;
            }
        };
    }
*/

}
