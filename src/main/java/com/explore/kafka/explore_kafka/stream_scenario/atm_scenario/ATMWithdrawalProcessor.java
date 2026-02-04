package com.explore.kafka.explore_kafka.stream_scenario.atm_scenario;

import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.aggregator.ATMBalanceAggregator;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.aggregator.CustomerTransactionAggregator;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.AtmState;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.CustomerTransactionWindow;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.WithdrawalRequest;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.model.WithdrawalResponse;
import com.explore.kafka.explore_kafka.stream_scenario.atm_scenario.serdes.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Properties;

public class ATMWithdrawalProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ATMWithdrawalProcessor.class);

    private static final String INPUT_TOPIC = "withdrawal-requests";

    private static final String ATM_BALANCE_STORE = "atm-balance-store";
    private static final String CUSTOMER_WINDOW_STORE = "customer-rate-limit-store";
    private static final String OUTPUT_TOPIC = "withdrawal-responses";

    public static void main(String[] args) {
        Properties props = buildStreamProperties();

        StreamsBuilder builder = new StreamsBuilder();
        buildATMValidationTopology(builder);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down ATM Withdrawal Processor...");
            streams.close(Duration.ofSeconds(10));
        }));

        // Start processing
        streams.start();
        logger.info("ATM Withdrawal Processor started successfully");
    }

    private static Properties buildStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "atm-withdrawal-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // CRITICAL: Exactly-once semantics
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        // State management
        props.put(StreamsConfig.STATE_DIR_CONFIG, "C:/kafka-streams-state");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Performance
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024);

        return props;
    }

    static void buildATMValidationTopology(StreamsBuilder builder) {
        var requestSerde = JsonSerdes.serdeFor(WithdrawalRequest.class);
        var atmSerde = JsonSerdes.serdeFor(AtmState.class);
        var customerTransactionSerde = JsonSerdes.serdeFor(CustomerTransactionWindow.class);
        var withdrawalResponse = JsonSerdes.serdeFor(WithdrawalResponse.class);

        KStream<String, WithdrawalRequest> requests = builder
                .stream(INPUT_TOPIC, Consumed.with(Serdes.String(), requestSerde))
                .peek((key, value) -> {
                    logger.info("Received request: {} for customer {} at ATM {}",
                            value.getRequestId(), value.getCustomerId(), value.getAtmId());
                });

        KStream<String, WithdrawalRequest> requestsFromAtm = requests
                .selectKey((key, value) -> value.getAtmId())
                .peek((atmId, request) -> logger.debug("Re-keyed request {} to ATM {}", request.getRequestId(), atmId));

        KGroupedStream<String, WithdrawalRequest> groupedByATM = requestsFromAtm
                .groupByKey(Grouped.with(Serdes.String(), requestSerde));

        KTable<String, AtmState> atmBalances = groupedByATM.aggregate(
                () -> null,
                new ATMBalanceAggregator(),
                Materialized.<String, AtmState, KeyValueStore<Bytes, byte[]>>as(ATM_BALANCE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(atmSerde)
                        .withCachingEnabled()
                        .withLoggingEnabled(new HashMap<>())
        );

        KStream<String, AtmState> atmValidatedByRequest = atmBalances.toStream()
                .selectKey((atmId, value) -> value.getRequestId())
                .peek((requestId, state) ->
                        logger.info("  ATM Validation: {} | Status: {} | Reason: {} | ATM Balance: ${}",
                                requestId, state.getStatus(), state.getStatus(), state.getCurrentBalance()));


        KStream<String, WithdrawalRequest> requestsFromCustomer = requests
                .selectKey((key, value) -> value.getCustomerId())
                .peek((customerId, request) ->
                        logger.debug("Customer Stream: Processing {} for customer {}",
                                request.getRequestId(), customerId));

        KTable<String, CustomerTransactionWindow> customerValidationResult = requestsFromCustomer.groupByKey(Grouped.with(Serdes.String(), requestSerde))
                .aggregate(() -> null,
                        new CustomerTransactionAggregator(),
                        Materialized.<String, CustomerTransactionWindow, KeyValueStore<Bytes, byte[]>>as(CUSTOMER_WINDOW_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(customerTransactionSerde)
                                .withCachingEnabled()
                                .withLoggingEnabled(new HashMap<>()));

        KStream<String, CustomerTransactionWindow> customerValidatedByRequest = customerValidationResult.toStream()
                .selectKey((customerId, value) -> value.getRequestId())
                .peek((requestId, window) ->
                        logger.info("  Customer Validation: {} | Status: {} | Txn Count: {}", requestId, window.getTransactionStatus(),
                                window.getTransactionCount()));


        KStream<String, WithdrawalResponse> responses = atmValidatedByRequest
                .outerJoin(customerValidatedByRequest,
                        (atmState, customerWindow) -> {
                            long joinStartTime = System.currentTimeMillis();
                            if (atmState != null && customerWindow != null) {
                                AtmState.Status atmStatus = atmState.getStatus();
                                String requestId = atmState.getRequestId();

                                boolean atmApproved = AtmState.Status.APPROVE.equals(atmState.getStatus());
                                boolean customerApproved = CustomerTransactionWindow.TransactionStatus.APPROVED.equals(customerWindow.getTransactionStatus());

                                if (atmApproved && customerApproved) {
                                    WithdrawalResponse processedWithdrawalResponse = new WithdrawalResponse();
                                    processedWithdrawalResponse.setRequestId(requestId);
                                    processedWithdrawalResponse.setCustomerId(customerWindow.getCustomerId());
                                    processedWithdrawalResponse.setAtmId(atmState.getAtmId());
                                    processedWithdrawalResponse.setRequestedAmount(customerWindow.getWithdrawalAmount());
                                    processedWithdrawalResponse.setCustomerTxnCountAfterLastTransaction(customerWindow.getTransactionCount());
                                    processedWithdrawalResponse.setStatus(WithdrawalResponse.Status.APPROVED);
                                    processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                    processedWithdrawalResponse.setBalance(customerWindow.getBalance());
                                    return processedWithdrawalResponse;
                                } else {
                                    String denialReason;
                                    if (!atmApproved) {
                                        denialReason = atmState.getStatus().toString();
                                    } else {
                                        denialReason = customerWindow.getTransactionStatus().toString();
                                    }
                                    WithdrawalResponse processedWithdrawalResponse = new WithdrawalResponse();
                                    processedWithdrawalResponse.setRequestId(requestId);
                                    processedWithdrawalResponse.setCustomerId(customerWindow.getCustomerId());
                                    processedWithdrawalResponse.setAtmId(atmState.getAtmId());
                                    processedWithdrawalResponse.setRequestedAmount(customerWindow.getWithdrawalAmount());
                                    //processedWithdrawalResponse.setCustomerTxnCountAfterLastTransaction(customerWindow.getTransactionCount());
                                    processedWithdrawalResponse.setStatus(WithdrawalResponse.Status.REJECTED);
                                    processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                    //processedWithdrawalResponse.setBalance(customerWindow.getBalance());
                                    processedWithdrawalResponse.setDenialReason(denialReason);
                                    return processedWithdrawalResponse;
                                }
                            }
                            if (atmState != null) {
                                logger.error("JOIN ERROR: Customer validation missing for request {}",
                                        atmState.getRequestId());
                                WithdrawalResponse processedWithdrawalResponse = new WithdrawalResponse();
                                processedWithdrawalResponse.setRequestId(atmState.getRequestId());
                                //processedWithdrawalResponse.setCustomerId(customerWindow.getCustomerId());
                                processedWithdrawalResponse.setAtmId(atmState.getAtmId());
                                //processedWithdrawalResponse.setRequestedAmount(customerWindow.getWithdrawalAmount());
                                //processedWithdrawalResponse.setCustomerTxnCountAfterLastTransaction(customerWindow.getTransactionCount());
                                processedWithdrawalResponse.setStatus(WithdrawalResponse.Status.REJECTED);
                                processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                //processedWithdrawalResponse.setBalance(customerWindow.getBalance());
                                processedWithdrawalResponse.setDenialReason("SYSTEM_ERROR_CUSTOMER_VALIDATION_TIMEOUT");
                                processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                return processedWithdrawalResponse;
                            }
                            if (customerWindow != null) {
                                logger.error("JOIN ERROR: ATM validation missing for request {}",
                                        customerWindow.getRequestId());
                                WithdrawalResponse processedWithdrawalResponse = new WithdrawalResponse();
                                processedWithdrawalResponse.setRequestId(customerWindow.getRequestId());
                                processedWithdrawalResponse.setCustomerId(customerWindow.getCustomerId());
                                processedWithdrawalResponse.setStatus(WithdrawalResponse.Status.REJECTED);
                                processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                processedWithdrawalResponse.setDenialReason("SYSTEM_ERROR_ATM_VALIDATION_TIMEOUT");
                                processedWithdrawalResponse.setTimeStamp(System.currentTimeMillis());
                                return processedWithdrawalResponse;
                            }

                            // ───────────────────────────────────────────────────────
                            // CASE 4: Both null (should never happen)
                            // ───────────────────────────────────────────────────────
                            logger.error("CRITICAL: Both validations null - this should never happen");
                            return new WithdrawalResponse();
                        },
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
                        StreamJoined.with(Serdes.String(), atmSerde, customerTransactionSerde)
                                .withName("atm-withdrawal-join") // Names the repartition topic
                                .withStoreName("atm-withdrawal-store")
                )
                .filter((key, value) -> value != null)
                .peek((requestId, response) -> logger.info("FINAL DECISION: {} | {}",
                        requestId, response.toString()));

        responses.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), withdrawalResponse));

        logger.info("Complete topology built successfully with ATM + Customer validation + Join");
    }

}
