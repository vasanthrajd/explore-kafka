package com.explore.kafka.explore_kafka.producer.service;

import com.explore.kafka.explore_kafka.producer.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface ProducerService {

    CompletableFuture<Long> publishMessageToKafkaTopic(String key, String value, Integer partitionId);

    CompletableFuture<Long> pushOrderEventToKafkaTopic(OrderEvent orderEvent);

    CompletableFuture<Long> pushOrderEventToKafkaTopicUsingAvro(OrderEvent orderEvent) throws IOException;

    CompletableFuture<Long> pushOrderEventToKafkaTopicUsingAvroTransaction(OrderEvent orderEvent) throws IOException;
}
