package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class WithdrawalRequest {
    private String requestId;
    private String customerId;
    private String atmId;
    private Long amount;
    private Long timeStamp;
}
