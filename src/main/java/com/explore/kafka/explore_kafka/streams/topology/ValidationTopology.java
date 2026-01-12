package com.explore.kafka.explore_kafka.streams.topology;

import com.explore.kafka.explore_kafka.streams.dto.OrderEvent;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValidationTopology {

    @Bean
    public KStream<String, OrderEvent> validationStream(
            StreamsBuilder builder,
            Serde<OrderEvent> orderSerde) {

        KStream<String, OrderEvent> orders =
                builder.stream("orders",
                        Consumed.with(Serdes.String(), orderSerde));

        Map<String, KStream<String, OrderEvent>> branches = orders.split(Named.as("order-event-"))
                .branch((k, o) -> o.quantity()> 2,
                        Branched.as("valid"))
                .defaultBranch(Branched.as("invalid"));

        branches.get("order-event-valid")
                    .to("valid-orders");
        branches.get("order-event-invalid")
                .to("invalid-orders");

        return branches.get("order-event-valid");
    }
}
