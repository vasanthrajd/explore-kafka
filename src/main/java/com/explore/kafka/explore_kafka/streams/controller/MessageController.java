package com.explore.kafka.explore_kafka.streams.controller;


import com.explore.kafka.explore_kafka.consumer.dto.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Value("${kafka.topics.input}")
    private String inputTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MessageController(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody OrderEvent orderEvent) {
        try {

            String messageJson = objectMapper.writeValueAsString(orderEvent);
            kafkaTemplate.send(inputTopic, orderEvent.orderId(), messageJson);
            log.info("Message sent to topic: {} with key: {}", inputTopic, orderEvent.orderId());
            return "Message sent successfully";
        } catch (Exception e) {
            log.error("Error sending orderEvent", e);
            return "Error sending orderEvent: " + e.getMessage();
        }
    }
}
