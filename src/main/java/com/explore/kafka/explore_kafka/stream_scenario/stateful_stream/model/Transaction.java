package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class Transaction {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("transaction_type")
    private String transactionType; // CREDIT or DEBIT

    @JsonProperty("timestamp")
    private long timestamp;

    // Default constructor for Jackson
    public Transaction() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public Transaction(String transactionId, String customerId, double amount, String transactionType) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = Instant.now().toEpochMilli();
    }
}
