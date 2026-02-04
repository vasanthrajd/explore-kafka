package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.aggregator;

import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.CustomerTransactionWindow;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.WithdrawalRequest;
import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerTransactionAggregator implements Aggregator<String, WithdrawalRequest, CustomerTransactionWindow> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerTransactionAggregator.class);

    private static final long INITIAL_CUSTOMER_BALANCE = 350000l;


    @Override
    public CustomerTransactionWindow apply(String customerId, WithdrawalRequest withdrawalRequest,
                                           CustomerTransactionWindow customerTransactionWindow) {
        if (customerTransactionWindow == null) {
            logger.info("Setting up Customer: {} with current withdrawal count  total of ${} dollars",
                    customerId, INITIAL_CUSTOMER_BALANCE);
            customerTransactionWindow = new CustomerTransactionWindow(customerId, new ArrayList<>(), 0l, INITIAL_CUSTOMER_BALANCE,
                    0);
        }

        long requestTime = withdrawalRequest.getTimeStamp();
        long tenMinutesAgo = requestTime - (10 * 60 * 1000);
        long oneMinuteAgo = requestTime - (1 * 60 * 1000);
        customerTransactionWindow.setRequestId(withdrawalRequest.getRequestId());
        customerTransactionWindow.setWithdrawalAmount(withdrawalRequest.getAmount());
        List<Long> recentTimestamps = customerTransactionWindow.getTransactionTimeStamps()
                .stream()
                .filter(ts -> ts > tenMinutesAgo)
                .collect(Collectors.toList());

        long countInLastMinute = recentTimestamps.stream()
                .filter(ts -> ts > oneMinuteAgo)
                .count();

        long countInLast10Minutes = recentTimestamps.size();
        if (countInLastMinute >= 5) {
            // DENY - too many in 1 minute
            // DON'T add timestamp
            // Set denial reason
            // Return window
            customerTransactionWindow.setTransactionStatus(CustomerTransactionWindow.TransactionStatus.TOO_MANY_IN_1_MIN);
            return customerTransactionWindow;

        }

        if (countInLast10Minutes >= 7) {
            // DENY - too many in 10 minutes
            // DON'T add timestamp
            // Set denial reason
            // Return window
            customerTransactionWindow.setTransactionStatus(CustomerTransactionWindow.TransactionStatus.TRANSACTION_EXCEED);
            return customerTransactionWindow;
        }
        if (customerTransactionWindow.getBalance() < withdrawalRequest.getAmount()) {
            customerTransactionWindow.setTransactionStatus(CustomerTransactionWindow.TransactionStatus.LOW_BALANCE);
            return customerTransactionWindow;
        }

        // STEP 6: APPROVE - Add current timestamp
        recentTimestamps.add(requestTime);
        // Update window
        customerTransactionWindow.setTransactionTimeStamps(recentTimestamps);
        customerTransactionWindow.setLastTransactionTime(requestTime);
        customerTransactionWindow.setBalance(customerTransactionWindow.getBalance()-withdrawalRequest.getAmount());
        customerTransactionWindow.setTransactionStatus(CustomerTransactionWindow.TransactionStatus.APPROVED);
        customerTransactionWindow.setTransactionCount(recentTimestamps.size());
        return customerTransactionWindow;
    }
}
