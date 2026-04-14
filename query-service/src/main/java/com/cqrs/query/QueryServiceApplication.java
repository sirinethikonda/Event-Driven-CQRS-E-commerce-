package com.cqrs.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableKafkaStreams
public class QueryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueryServiceApplication.class, args);
    }

    @Bean
    public NewTopic productEventsTopic(@Value("${topics.product-events:product-events}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderEventsTopic(@Value("${topics.order-events:order-events}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }
}
