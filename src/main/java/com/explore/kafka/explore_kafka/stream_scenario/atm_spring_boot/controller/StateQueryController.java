package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.controller;

import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.configuration.KafkaStreamsConfig;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.AtmState;
import com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot.model.CustomerTransactionWindow;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/state")
public class StateQueryController {

    private final KafkaStreams streams;

    private final KafkaStreamsConfig kafkaStreamsConfig;

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;


    public StateQueryController(KafkaStreams streams, KafkaStreamsConfig kafkaStreamsConfig, StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streams = streams;
        this.kafkaStreamsConfig = kafkaStreamsConfig;
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public boolean getStreamState() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        return kafkaStreams != null && kafkaStreams.state().isRunningOrRebalancing();
    }

    @GetMapping("/atm/{atmId}")
    public ResponseEntity<?> getAtmState(@PathVariable String atmId) {
        if (getStreamState()) {
            ReadOnlyKeyValueStore<String, AtmState> store =
                    streamsBuilderFactoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(KafkaStreamsConfig.ATM_BALANCE_STORE, QueryableStoreTypes.keyValueStore()));

            AtmState value = store.get(atmId);
            return ResponseEntity.of(Optional.ofNullable(value));
        }
        return ResponseEntity.status(503).body("Stream is in not running state");
    }

    @GetMapping("/atm")
    public ResponseEntity<List<?>> listAllAtmStates() {
        if (getStreamState()) {
            ReadOnlyKeyValueStore<String, AtmState> store =
                    streamsBuilderFactoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(KafkaStreamsConfig.ATM_BALANCE_STORE, QueryableStoreTypes.keyValueStore()));
            List<AtmState> all = new ArrayList<>();
            try (KeyValueIterator<String, AtmState> iter = store.all()) {
                while (iter.hasNext()) {
                    all.add(iter.next().value);
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(all);
        }
        return ResponseEntity.status(503).body(List.of("Stream is in not running state"));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerWindow(@PathVariable String customerId) {
        if (getStreamState()) {
            ReadOnlyKeyValueStore<String, CustomerTransactionWindow> store =
                    streamsBuilderFactoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(KafkaStreamsConfig.CUSTOMER_WINDOW_STORE, QueryableStoreTypes.keyValueStore()));
            CustomerTransactionWindow value = store.get(customerId);
            return ResponseEntity.of(Optional.ofNullable(value));
        }
        return ResponseEntity.status(503).body("Stream is in not running state");
    }

    @GetMapping("/customer")
    public ResponseEntity<List<?>> listAllCustomerWindows() {
        if (getStreamState()) {
            ReadOnlyKeyValueStore<String, CustomerTransactionWindow> store =
                    streamsBuilderFactoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(KafkaStreamsConfig.CUSTOMER_WINDOW_STORE, QueryableStoreTypes.keyValueStore()));
            List<CustomerTransactionWindow> all = new ArrayList<>();
            try (KeyValueIterator<String, CustomerTransactionWindow> iter = store.all()) {
                while (iter.hasNext()) {
                    all.add(iter.next().value);
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(all);
        }
        return ResponseEntity.status(503).body(List.of("Stream is in not running state"));
    }
}
