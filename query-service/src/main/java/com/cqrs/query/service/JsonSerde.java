package com.cqrs.query.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerde<T> implements Serde<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> targetClass;

    public JsonSerde(Class<T> targetClass, ObjectMapper objectMapper) {
        this.targetClass = targetClass;
        this.objectMapper = objectMapper;
    }

    @Override
    public Serializer<T> serializer() {
        return (topic, data) -> {
            try {
                if (data == null) return null;
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing JSON message", e);
            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return (topic, data) -> {
            try {
                if (data == null) return null;
                return objectMapper.readValue(data, targetClass);
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing JSON message", e);
            }
        };
    }
}
