package com.cqrs.query.service;

import com.cqrs.query.dto.Events;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsTopology {

    public static final String PRODUCT_SALES_STORE = "product-sales-store";
    public static final String CATEGORY_REVENUE_STORE = "category-revenue-store";
    public static final String HOURLY_SALES_STORE = "hourly-sales-store";

    private final ObjectMapper objectMapper;

    @Value("${topics.product-events}")
    private String productEventsTopic;

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {

        // 1. KTable for Products
        KTable<String, Events.ProductPayload> productTable = streamsBuilder
                .stream(productEventsTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> parseEvent(value, new TypeReference<Events.EventWrapper>() {}))
                .filter((key, wrapper) -> "ProductCreated".equals(wrapper.getEventType()))
                .mapValues(wrapper -> objectMapper.convertValue(wrapper.getPayload(), Events.ProductPayload.class))
                .toTable(Materialized.with(Serdes.String(), new JsonSerde<>(Events.ProductPayload.class, objectMapper)));

        // 2. KStream for Orders
        KStream<String, Events.OrderPayload> orderStream = streamsBuilder
                .stream(orderEventsTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> parseEvent(value, new TypeReference<Events.EventWrapper>() {}))
                .filter((key, wrapper) -> "OrderCreated".equals(wrapper.getEventType()))
                .mapValues(wrapper -> objectMapper.convertValue(wrapper.getPayload(), Events.OrderPayload.class));

        // 3. FlatMap OrderItems
        KStream<String, Events.OrderItem> orderItemStream = orderStream
                .flatMap((orderId, order) -> {
                    List<KeyValue<String, Events.OrderItem>> items = new LinkedList<>();
                    if (order != null && order.getItems() != null) {
                        for (Events.OrderItem item : order.getItems()) {
                            items.add(new KeyValue<>(String.valueOf(item.getProductId()), item));
                        }
                    }
                    return items;
                });

        // 4. Enrich with Product categories (Requirement 8)
        KStream<String, Events.EnrichedOrderItem> enrichedOrderItemStream = orderItemStream
                .join(productTable,
                        (orderItem, product) -> Events.EnrichedOrderItem.builder()
                                .productId(orderItem.getProductId())
                                .quantity(orderItem.getQuantity())
                                .price(orderItem.getPrice())
                                .category(product != null ? product.getCategory() : "UNKNOWN")
                                .build(),
                        Joined.with(Serdes.String(),
                                new JsonSerde<>(Events.OrderItem.class, objectMapper),
                                new JsonSerde<>(Events.ProductPayload.class, objectMapper)));

        // 5. Product Sales Aggregation (Requirement 9)
        enrichedOrderItemStream
                .map((key, item) -> new KeyValue<>(String.valueOf(item.getProductId()), item.getPrice().doubleValue() * item.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(PRODUCT_SALES_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()));

        // 6. Category Revenue Aggregation (Requirement 10)
        enrichedOrderItemStream
                .map((key, item) -> new KeyValue<>(item.getCategory(), item.getPrice().doubleValue() * item.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(CATEGORY_REVENUE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()));

        // 7. Hourly Sales Aggregation (Requirement 11)
        Duration windowSize = Duration.ofHours(1);
        Duration gracePeriod = Duration.ofMinutes(10);
        
        enrichedOrderItemStream
                .map((key, item) -> new KeyValue<>("GLOBAL_SALES", item.getPrice().doubleValue() * item.getQuantity()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(TimeWindows.ofSizeAndGrace(windowSize, gracePeriod))
                .reduce(Double::sum,
                        Materialized.<String, Double, WindowStore<Bytes, byte[]>>as(HOURLY_SALES_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()));
    }

    private <T> T parseEvent(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Failed to parse event", e);
            return null;
        }
    }
}
