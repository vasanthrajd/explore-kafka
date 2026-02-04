package com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class CustomerTransactionWindow {
    private String customerId;
    private List<Long> transactionTimeStamps;
    private Long lastTransactionTime;
    private Long balance;
    private Integer transactionCount;
    private TransactionStatus transactionStatus;

    private String requestId;
    private Long withdrawalAmount;

    public CustomerTransactionWindow(String customerId, List<Long> transactionTimeStamps, Long lastTransactionTime, Long balance, Integer transactionCount) {
        this.customerId = customerId;
        this.transactionTimeStamps = transactionTimeStamps;
        this.lastTransactionTime = lastTransactionTime;
        this.balance = balance;
        this.transactionCount = transactionCount;
    }

    public enum TransactionStatus {
        APPROVED,
        LOW_BALANCE,
        TOO_MANY_IN_1_MIN,
        TRANSACTION_EXCEED
    }
}
