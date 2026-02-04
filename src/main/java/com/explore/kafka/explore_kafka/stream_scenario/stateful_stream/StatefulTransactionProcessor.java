package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream;

import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model.AccountBalance;
import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.model.Transaction;
import com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.serdes.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
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

public class StatefulTransactionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StatefulTransactionProcessor.class);

    private static final String INPUT_TOPIC = "transactions";
    private static final String OUTPUT_TOPIC = "account-balances";
    private static final String STATE_STORE_NAME = "account-balance-store";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stateful-transaction-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // CRITICAL: Enable exactly-once semantics
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        // State store configuration
        props.put(StreamsConfig.STATE_DIR_CONFIG, "D:\\vasanth-git\\explore-kafka\\src\\main\\resources\\kafka-streams-state");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000); // Commit every 1 second

        // Performance tuning
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024); // 10MB cache
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);

        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Kafka Streams application...");
            streams.close(Duration.ofSeconds(20));
        }));

        streams.start();
        logger.info("Kafka Streams application started successfully");
    }

    static void buildTopology(StreamsBuilder builder) {
        // Define serdes
        var transactionSerde = JsonSerdes.serdeFor(Transaction.class);
        var balanceSerde = JsonSerdes.serdeFor(AccountBalance.class);

        // Input stream: transactions keyed by customer_id
        KStream<String, Transaction> transactions = builder
                .stream(INPUT_TOPIC, Consumed.with(Serdes.String(), transactionSerde))
                .peek((key, value) -> logger.info("Processing transaction: {}", value));

        // Aggregate transactions into account balances using state store
        KTable<String, AccountBalance> accountBalances = transactions
                .groupByKey(Grouped.with(Serdes.String(), transactionSerde))
                .aggregate(
                        // Initializer: create new AccountBalance when first transaction arrives
                        () -> new AccountBalance("", 0.0, 0L, 0L),

                        // Aggregator: update balance with each new transaction
                        (customerId, transaction, balance) -> {
                            logger.info("Aggregating transaction for customer: {}", customerId);

                            balance.setCustomerId(customerId);
                            balance.setTransactionCount(balance.getTransactionCount() + 1);
                            balance.setLastUpdated(transaction.getTimestamp());

                            if ("CREDIT".equals(transaction.getTransactionType())) {
                                balance.setBalance(balance.getBalance() + transaction.getAmount());
                            } else if ("DEBIT".equals(transaction.getTransactionType())) {
                                balance.setBalance(balance.getBalance() - transaction.getAmount());
                            }

                            logger.info("Updated balance for {}: {}", customerId, balance.getBalance());
                            return balance;
                        },

                        // Materialized: configure the state store
                        Materialized.<String, AccountBalance, KeyValueStore<org.apache.kafka.common.utils.Bytes, byte[]>>as(STATE_STORE_NAME)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(balanceSerde)
                                .withCachingEnabled() // Enable caching for performance
                                .withLoggingEnabled(new HashMap<>())
                );

        // Output the balances to output topic
        accountBalances.toStream()
                .peek((key, value) -> logger.info("Publishing balance update: {} -> {}", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), balanceSerde));
    }
}