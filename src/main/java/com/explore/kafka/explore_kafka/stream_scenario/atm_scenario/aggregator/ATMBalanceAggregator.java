package com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.aggregator;

import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.AtmState;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.WithdrawalRequest;
import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ATMBalanceAggregator implements Aggregator<String, WithdrawalRequest, AtmState> {

    private static final Logger logger = LoggerFactory.getLogger(ATMBalanceAggregator.class);
    private static final long MAX_WITHDRAWAL_AMOUNT = 20000;
    private static final long INITIAL_ATM_BALANCE = 1000000;

    @Override
    public AtmState apply(String atmId, WithdrawalRequest withdrawalRequest, AtmState currentState) {
        if (currentState == null) {
            logger.info("Setting up ATM: {} with Currency total of ${} dollars", atmId, INITIAL_ATM_BALANCE);
            currentState = new AtmState(atmId, INITIAL_ATM_BALANCE, 0l, 0l, 0l, AtmState.Status.OPERATIONAL);
        }
        
        AtmState updatedState = createCopy(currentState);
        updatedState.setRequestId(withdrawalRequest.getRequestId());
        long requestedAmount = withdrawalRequest.getAmount();
        String requestId = withdrawalRequest.getRequestId();

        logger.info("Processing request {} for ATM {} | Requested: ${} | Current Balance: ${}",
                requestId, atmId, requestedAmount, currentState.getCurrentBalance());

        // STEP 3: VALIDATION RULE 1 - Check if amount exceeds maximum limit
        if (requestedAmount > MAX_WITHDRAWAL_AMOUNT) {
            logger.warn("DENIED - Amount exceeds limit: Request {} for ${} (max: ${})",
                    requestId, requestedAmount, MAX_WITHDRAWAL_AMOUNT);

            // Mark as denied but don't update balance
            updatedState.setStatus(AtmState.Status.DENIED);
            //updatedState.setDenialReason("AMOUNT_EXCEEDS_LIMIT");
            updatedState.setLastTransactionTime(withdrawalRequest.getTimeStamp());

            return updatedState;
        }

        // STEP 4: VALIDATION RULE 2 - Check if ATM has sufficient balance
        if (requestedAmount > currentState.getCurrentBalance()) {
            logger.warn("DENIED - Insufficient ATM funds: Request {} for ${} (available: ${})",
                    requestId, requestedAmount, currentState.getCurrentBalance());

            // Mark as denied but don't update balance
            updatedState.setStatus(AtmState.Status.DENIED);
           // updatedState.setDenialReason("INSUFFICIENT_ATM_FUNDS");
            updatedState.setLastTransactionTime(withdrawalRequest.getTimeStamp());

            // Check if ATM is running low (< 10% of initial balance)
            if (currentState.getCurrentBalance() < (INITIAL_ATM_BALANCE * 0.1)) {
                updatedState.setStatus(AtmState.Status.LOW_CASH);
                logger.error("ATM {} is running LOW on cash: ${}", atmId, currentState.getCurrentBalance());
            }

            return updatedState;
        }

        // STEP 5: APPROVAL - Deduct amount from balance
        long newBalance = currentState.getCurrentBalance() - requestedAmount;
        long newTotalDispensed = currentState.getTotalDispensed() + requestedAmount;
        long newTransactionCount = currentState.getTransactionCount() + 1;

        updatedState.setCurrentBalance(newBalance);
        updatedState.setTotalDispensed(newTotalDispensed);
        updatedState.setTransactionCount(newTransactionCount);
        updatedState.setLastTransactionTime(withdrawalRequest.getTimeStamp());
        updatedState.setStatus(AtmState.Status.APPROVE);
        //updatedState.setDenialReason(null);

        logger.info("APPROVED - Request {} | New ATM Balance: ${} | Total Dispensed: ${} | Txn Count: {}",
                requestId, newBalance, newTotalDispensed, newTransactionCount);

        // STEP 6: Check if ATM needs refill alert
        if (newBalance < (INITIAL_ATM_BALANCE * 0.1)) {
            updatedState.setStatus(AtmState.Status.LOW_CASH);
            logger.warn("ATM {} needs refill - Balance: ${}", atmId, newBalance);
        }

        return updatedState;
    }

    private AtmState createCopy(AtmState original) {
        AtmState copy = new AtmState();
        copy.setAtmId(original.getAtmId());
        copy.setCurrentBalance(original.getCurrentBalance());
        copy.setTotalDispensed(original.getTotalDispensed());
        copy.setTransactionCount(original.getTransactionCount());
        copy.setLastTransactionTime(original.getLastTransactionTime());
        copy.setStatus(original.getStatus());
        return copy;
    }


}
