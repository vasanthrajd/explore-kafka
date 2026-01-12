package com.explore.kafka.explore_kafka.consumer.dto;

public record OrderEventStatus(String orderId, String productName, Integer quantity, String made,
                               String confirmation) {
}
