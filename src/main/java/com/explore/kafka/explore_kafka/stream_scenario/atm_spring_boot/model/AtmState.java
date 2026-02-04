package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AtmState {
    private String atmId;
    private Long currentBalance;
    private Long totalDispensed;
    private Long transactionCount;
    private Long lastTransactionTime;
    private Status status;

    private String requestId;

    public enum Status {
        OPERATIONAL,
        LOW_CASH,
        DENIED,
        OUT_OF_SERVICE, APPROVE;
    }

    public AtmState(String atmId, Long currentBalance, Long totalDispensed, Long transactionCount, Long lastTransactionTime, Status status) {
        this.atmId = atmId;
        this.currentBalance = currentBalance;
        this.totalDispensed = totalDispensed;
        this.transactionCount = transactionCount;
        this.lastTransactionTime = lastTransactionTime;
        this.status = status;
    }
}
