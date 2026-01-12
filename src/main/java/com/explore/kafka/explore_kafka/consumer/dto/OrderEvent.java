package com.explore.kafka.explore_kafka.consumer.dto;

public record OrderEvent(String orderId, String productName, Integer quantity) {
}
