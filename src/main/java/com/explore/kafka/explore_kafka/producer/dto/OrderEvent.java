package com.explore.kafka.explore_kafka.producer.dto;

public record OrderEvent(String orderId, String productName, Integer quantity, String made) {
}
