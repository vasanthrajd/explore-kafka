// java
package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.configuration;

import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.aggregator.ATMBalanceAggregator;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.aggregator.CustomerTransactionAggregator;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.AtmState;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.CustomerTransactionWindow;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.WithdrawalRequest;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.WithdrawalResponse;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.serdes.JsonSerdes;
import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.StatefulTransactionProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Configuration
@Slf4j
public class KafkaStreamsConfig {
    public static final String INPUT_TOPIC = "withdrawal-requests";
    public static final String OUTPUT_TOPIC = "withdrawal-responses";
    public static final String ATM_BALANCE_STORE = "atm-balance-store";
    public static final String CUSTOMER_WINDOW_STORE = "customer-rate-limit-store";

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class);
    // java
    @Bean
    public Topology atmTopology(StreamsBuilder builder) {
        final var keySerde = Serdes.String();

        final var requestSerde = JsonSerdes.serdeFor(WithdrawalRequest.class);
        final var atmSerde = JsonSerdes.serdeFor(AtmState.class);
        final var customerTransactionSerde = JsonSerdes.serdeFor(CustomerTransactionWindow.class);
        final var withdrawalResponse = JsonSerdes.serdeFor(WithdrawalResponse.class);
        final var emptyLogConfig = Collections.<String, String>emptyMap();


        // original stream
        KStream<String, WithdrawalRequest> requests =
                builder.stream(INPUT_TOPIC, Consumed.with(keySerde, requestSerde));

        // group by ATM id (rekey+group in one step)
        KGroupedStream<String, WithdrawalRequest> groupedByATM =
                requests.groupBy((k, v) -> v.getAtmId(), Grouped.with(keySerde, requestSerde));

        KTable<String, AtmState> atmBalances = groupedByATM.aggregate(
                () -> null,
                new ATMBalanceAggregator(),
                Materialized.<String, AtmState, KeyValueStore<Bytes, byte[]>>as(ATM_BALANCE_STORE)
                        .withKeySerde(keySerde)
                        .withValueSerde(atmSerde)
                        .withCachingEnabled()
                        .withLoggingEnabled(emptyLogConfig)
        );

        // rekey by requestId for joining
        KStream<String, AtmState> atmValidatedByRequest = atmBalances.toStream()
                .selectKey((atmId, value) -> value.getRequestId());

        // group by customer id (rekey+group in one step)
        KGroupedStream<String, WithdrawalRequest> groupedByCustomer =
                requests.groupBy((k, v) -> v.getCustomerId(), Grouped.with(keySerde, requestSerde));

        KTable<String, CustomerTransactionWindow> customerValidationResult = groupedByCustomer.aggregate(
                () -> null,
                new CustomerTransactionAggregator(),
                Materialized.<String, CustomerTransactionWindow, KeyValueStore<Bytes, byte[]>>as(CUSTOMER_WINDOW_STORE)
                        .withKeySerde(keySerde)
                        .withValueSerde(customerTransactionSerde)
                        .withCachingEnabled()
                        .withLoggingEnabled(emptyLogConfig)
        );

        KStream<String, CustomerTransactionWindow> customerValidatedByRequest = customerValidationResult.toStream()
                .selectKey((customerId, value) -> value.getRequestId());

        KStream<String, WithdrawalResponse> responses = atmValidatedByRequest
                .outerJoin(
                        customerValidatedByRequest,
                        (atmState, customerWindow) -> {
                            if (atmState != null && customerWindow != null) {
                                boolean atmApproved = AtmState.Status.APPROVE.equals(atmState.getStatus());
                                boolean customerApproved = CustomerTransactionWindow.TransactionStatus.APPROVED.equals(customerWindow.getTransactionStatus());
                                String requestId = atmState.getRequestId();

                                if (atmApproved && customerApproved) {
                                    return WithdrawalResponse.builder()
                                            .requestId(requestId)
                                            .customerId(customerWindow.getCustomerId())
                                            .atmId(atmState.getAtmId())
                                            .requestedAmount(customerWindow.getWithdrawalAmount())
                                            .customerTxnCountAfterLastTransaction(customerWindow.getTransactionCount())
                                            .status(WithdrawalResponse.Status.APPROVED)
                                            .timeStamp(System.currentTimeMillis())
                                            .balance(customerWindow.getBalance())
                                            .build();
                                } else {
                                    String denialReason = atmApproved ? customerWindow.getTransactionStatus().toString() : atmState.getStatus().toString();
                                    return WithdrawalResponse.builder()
                                            .requestId(requestId)
                                            .customerId(customerWindow.getCustomerId())
                                            .atmId(atmState.getAtmId())
                                            .requestedAmount(customerWindow.getWithdrawalAmount())
                                            .status(WithdrawalResponse.Status.REJECTED)
                                            .denialReason(denialReason)
                                            .timeStamp(System.currentTimeMillis())
                                            .build();

                                }
                            }
                            if (atmState != null && customerWindow == null) {
                                logger.error("JOIN ERROR: Customer validation missing for request {}",
                                        atmState.getRequestId());

                                return WithdrawalResponse.builder()
                                        .requestId(atmState.getRequestId())
                                        .customerId(customerWindow.getCustomerId())
                                        .atmId(atmState.getAtmId())
                                        .requestedAmount(customerWindow.getWithdrawalAmount())
                                        .status(WithdrawalResponse.Status.REJECTED)
                                        .denialReason("SYSTEM_ERROR_CUSTOMER_VALIDATION_TIMEOUT")
                                        .timeStamp(System.currentTimeMillis())
                                        .build();
                            }

                            // ───────────────────────────────────────────────────────
                            // CASE 3: Customer validation present, ATM missing
                            // ───────────────────────────────────────────────────────
                            if (atmState == null && customerWindow != null) {
                                logger.error("JOIN ERROR: ATM validation missing for request {}",
                                        customerWindow.getRequestId());

                                return WithdrawalResponse.builder()
                                        .requestId(customerWindow.getRequestId())
                                        .customerId(customerWindow.getCustomerId())
                                        .status(WithdrawalResponse.Status.REJECTED)
                                        .denialReason("SYSTEM_ERROR_ATM_VALIDATION_TIMEOUT")
                                        .timeStamp(System.currentTimeMillis())
                                        .build();
                            }

                            // ───────────────────────────────────────────────────────
                            // CASE 4: Both null (should never happen)
                            // ───────────────────────────────────────────────────────
                            logger.error("CRITICAL: Both validations null - this should never happen");
                            return null;
                        },
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
                        StreamJoined.with(keySerde, atmSerde, customerTransactionSerde)
                                .withName("atm-withdrawal-join") // Names the repartition topic
                                .withStoreName("atm-withdrawal-store")
                )
                .filter((key, value) -> value != null)
                .peek((requestId, response) -> logger.info("FINAL DECISION: {} | {}",
                            requestId, response.toString()));

        responses.to(OUTPUT_TOPIC, Produced.with(keySerde, withdrawalResponse));

        return builder.build();
    }

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;


    @Bean(initMethod = "start", destroyMethod = "close")
    public KafkaStreams kafkaStreams(Topology atmTopology) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "D:\\vasanth-git\\explore-kafka\\src\\main\\resources\\kafka-streams-state");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024);
        return new KafkaStreams(atmTopology, props);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}