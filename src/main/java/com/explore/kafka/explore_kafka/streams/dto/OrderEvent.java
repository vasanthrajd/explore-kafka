package com.explore.kafka.explore_kafka.streams.dto;

public record OrderEvent(String orderId, String productName, Integer quantity, String made) {
}
