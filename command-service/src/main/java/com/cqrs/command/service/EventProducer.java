package com.cqrs.command.service;

import com.cqrs.command.event.EventTypes;
import com.cqrs.command.domain.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.product-events}")
    private String productEventsTopic;

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    public void publishProductCreated(EventTypes.ProductPayload payload) {
        EventTypes.EventWrapper<EventTypes.ProductPayload> wrapper = new EventTypes.EventWrapper<>("ProductCreated", payload);
        kafkaTemplate.send(productEventsTopic, String.valueOf(payload.getId()), wrapper);
    }

    public void publishOrderCreated(Order order) {
        EventTypes.EventWrapper<Order> wrapper = new EventTypes.EventWrapper<>("OrderCreated", order);
        kafkaTemplate.send(orderEventsTopic, String.valueOf(order.getId()), wrapper);
    }

    public void publishOrderUpdated(Long orderId, String newStatus) {
        EventTypes.OrderStatusUpdate payload = new EventTypes.OrderStatusUpdate(orderId, newStatus);
        EventTypes.EventWrapper<EventTypes.OrderStatusUpdate> wrapper = new EventTypes.EventWrapper<>("OrderUpdated", payload);
        kafkaTemplate.send(orderEventsTopic, String.valueOf(orderId), wrapper);
    }
}
