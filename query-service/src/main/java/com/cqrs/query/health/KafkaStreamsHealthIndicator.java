package com.cqrs.query.health;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaStreamsHealthIndicator implements HealthIndicator {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Override
    public Health health() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            return Health.down().withDetail("reason", "Kafka Streams not initialized").build();
        }

        KafkaStreams.State state = kafkaStreams.state();
        if (state.isRunningOrRebalancing()) {
            return Health.up().withDetail("state", state.toString()).build();
        } else {
            return Health.down().withDetail("state", state.toString()).build();
        }
    }
}
