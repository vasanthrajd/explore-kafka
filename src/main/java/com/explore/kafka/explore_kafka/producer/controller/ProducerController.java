package com.explore.kafka.explore_kafka.producer.controller;

import com.explore.kafka.explore_kafka.producer.dto.OrderEvent;
import com.explore.kafka.explore_kafka.producer.configuration.KafkaProducerProperties;
import com.explore.kafka.explore_kafka.producer.configuration.ProducerProperties;
import com.explore.kafka.explore_kafka.producer.service.ProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/producer")
@Tag(name = "Producer API", description = "Operations related to Producer configuration")
public class ProducerController {

    private final ProducerProperties producerProperties;

    private final KafkaProducerProperties kafkaProducerProperties;

    private final ProducerService producerService;

    public ProducerController(ProducerProperties producerProperties,
                              KafkaProducerProperties kafkaProducerProperties,
                              ProducerService producerService) {
        this.producerProperties = producerProperties;
        this.kafkaProducerProperties = kafkaProducerProperties;
        this.producerService = producerService;
    }

    @GetMapping("/config")
    @Operation(
            summary = "Get Producer Configuration",
            description = "Returns the producer configuration loaded during application boot",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Producer configuration retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ProducerProperties.class))
                    )
            }
    )
    public ProducerProperties getProducerConfig() {
        return producerProperties;
    }

    @PostMapping("/push-message")
    @Operation(
            summary = "Post Message to Kafka Topic",
            description = "Post Message to Kafka Topic",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post Message to the Kafka Topic",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public CompletableFuture<ResponseEntity<Long>> pushMessageToKafka(@RequestBody Map<String, String> message) {
        return producerService.publishMessageToKafkaTopic(message.get("key"), message.get("value"), null)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(200).body(-1L));
    }

    @PostMapping("/push-message/{partitionid}")
    @Operation(
            summary = "Post Message to Kafka Topic",
            description = "Post Message to Kafka Topic",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post Message to the Kafka Topic",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public CompletableFuture<ResponseEntity<Long>> pushMessageToKafkaTopicByPartition(@PathVariable(name = "partitionid") Integer partitionid,
                                                                                      @RequestBody Map<String, String> message) {
        return producerService.publishMessageToKafkaTopic(message.get("key"), message.get("value"), partitionid)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(200).body(-1L));
    }

    @PostMapping("/push-order")
    @Operation(
            summary = "Post Order Json to Kafka Topic",
            description = "Post Order JSON to Kafka Topic",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post Order JSON to the Kafka Topic",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public CompletableFuture<ResponseEntity<Long>> pushOrderEventToKafkaTopic(@RequestBody OrderEvent orderEvent) {
        return producerService.pushOrderEventToKafkaTopic(orderEvent)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(200).body(-1L));
    }

    @PostMapping("/push-order-avro")
    @Operation(
            summary = "Post Order Json to Kafka Topic",
            description = "Post Order JSON to Kafka Topic",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post Order JSON to the Kafka Topic",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public CompletableFuture<ResponseEntity<Long>> pushOrderEventAvroToKafkaTopic(@RequestBody OrderEvent orderEvent) throws IOException {
        return producerService.pushOrderEventToKafkaTopicUsingAvro(orderEvent)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(200).body(-1L));
    }

    @PostMapping("/push-order-avro-transactional")
    @Operation(
            summary = "Post Order Json to Kafka Topic",
            description = "Post Order JSON to Kafka Topic",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post Order JSON to the Kafka Topic",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public CompletableFuture<ResponseEntity<Long>> pushOrderEventAvroToKafkaTopicTransactional(@RequestBody OrderEvent orderEvent) throws IOException {
        return producerService.pushOrderEventToKafkaTopicUsingAvroTransaction(orderEvent)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(200).body(-1L));
    }
}
