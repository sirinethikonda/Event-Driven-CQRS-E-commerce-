package com.cqrs.command.service;

import com.cqrs.command.domain.Order;
import com.cqrs.command.event.EventTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventProducer eventProducer;

    @Test
    void testPublishProductCreated() {
        ReflectionTestUtils.setField(eventProducer, "productEventsTopic", "product-events");
        EventTypes.ProductPayload payload = EventTypes.ProductPayload.builder().id(1L).build();
        
        eventProducer.publishProductCreated(payload);
        
        Mockito.verify(kafkaTemplate).send(eq("product-events"), eq("1"), any(EventTypes.EventWrapper.class));
    }

    @Test
    void testPublishOrderCreated() {
        ReflectionTestUtils.setField(eventProducer, "orderEventsTopic", "order-events");
        Order order = Order.builder().id(1L).build();
        
        eventProducer.publishOrderCreated(order);
        
        Mockito.verify(kafkaTemplate).send(eq("order-events"), eq("1"), any(EventTypes.EventWrapper.class));
    }

    @Test
    void testPublishOrderUpdated() {
        ReflectionTestUtils.setField(eventProducer, "orderEventsTopic", "order-events");
        
        eventProducer.publishOrderUpdated(1L, "PAID");
        
        Mockito.verify(kafkaTemplate).send(eq("order-events"), eq("1"), any(EventTypes.EventWrapper.class));
    }
}
