package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountBalance {
    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("balance")
    private double balance;

    @JsonProperty("transaction_count")
    private long transactionCount;

    @JsonProperty("last_updated")
    private long lastUpdated;


    public AccountBalance(String customerId, double balance, long transactionCount, long lastUpdated) {
        this.customerId = customerId;
        this.balance = balance;
        this.transactionCount = transactionCount;
        this.lastUpdated = lastUpdated;
    }
}
