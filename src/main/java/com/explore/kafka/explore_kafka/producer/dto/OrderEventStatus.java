package com.explore.kafka.explore_kafka.producer.dto;

public record OrderEventStatus(String orderId, String productName, Integer quantity, String made,
                               String confirmation) {
}
