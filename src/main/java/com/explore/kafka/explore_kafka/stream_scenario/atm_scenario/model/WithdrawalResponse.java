package com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model;

import lombok.Builder;
import lombok.Data;

@Data
public class WithdrawalResponse {
    private String requestId;
    private String customerId;
    private String atmId;
    private Long requestedAmount;
    private Status status;
    private String denialResponse;
    private Long balance;
    private Integer customerTxnCountAfterLastTransaction;
    private Long timeStamp;
    private String denialReason;
    public static enum Status {
        APPROVED,
        REJECTED
    }
}
