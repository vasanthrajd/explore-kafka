package com.explore.kafka.explore_kafka.stream_scenario.stateful_stream.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

public class JsonSerdes {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public static <T> Serde<T> serdeFor(Class<T> type) {
        return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(type));
    }

    static class JsonSerializer<T> implements Serializer<T> {
        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) return null;
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (IOException e) {
                throw new RuntimeException("Error serializing JSON", e);
            }
        }
    }

    static class JsonDeserializer<T> implements Deserializer<T> {
        private final Class<T> type;

        public JsonDeserializer(Class<T> type) {
            this.type = type;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) return null;
            try {
                return objectMapper.readValue(data, type);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing JSON", e);
            }
        }
    }

}
