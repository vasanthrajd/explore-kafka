package com.explore.kafka.explore_kafka.streams.configuration;

import com.explore.kafka.explore_kafka.streams.dto.OrderEvent;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;


@Configuration
public class SerdeConfig {
    @Bean
    public Serde<OrderEvent> orderSerde() {
        JacksonJsonSerializer<OrderEvent> serializer = new JacksonJsonSerializer<>();
        JacksonJsonDeserializer<OrderEvent> deserializer = new JacksonJsonDeserializer<>(OrderEvent.class);
        deserializer.addTrustedPackages("*");
        return Serdes.serdeFrom(serializer, deserializer);
    }

    /*@Bean
    public Serde<Customer> customerSerde() {
        JacksonJsonSerializer<Customer> serializer = new JacksonJsonSerializer<>();
        JacksonJsonDeserializer<Customer> deserializer = new JacksonJsonDeserializer<>(Customer.class);
        deserializer.addTrustedPackages("*");
        return Serdes.serdeFrom(serializer, deserializer);
    }

    @Bean
    public Serde<Product> productSerde() {
        JacksonJsonSerializer<Product> serializer = new JacksonJsonSerializer<>();
        JacksonJsonDeserializer<Product> deserializer = new JacksonJsonDeserializer<>(Product.class);
        deserializer.addTrustedPackages("*");
        return Serdes.serdeFrom(serializer, deserializer);
    }*/
}
