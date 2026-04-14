package com.cqrs.query.service;

import com.cqrs.query.dto.Events;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> productTopic;
    private TestInputTopic<String, String> orderTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        StreamsBuilder builder = new StreamsBuilder();
        AnalyticsTopology topologyBuilder = new AnalyticsTopology(objectMapper);
        
        ReflectionTestUtils.setField(topologyBuilder, "productEventsTopic", "product-events");
        ReflectionTestUtils.setField(topologyBuilder, "orderEventsTopic", "order-events");

        topologyBuilder.buildPipeline(builder);
        Topology topology = builder.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);

        productTopic = testDriver.createInputTopic("product-events", new StringSerializer(), new StringSerializer());
        orderTopic = testDriver.createInputTopic("order-events", new StringSerializer(), new StringSerializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testTopologyCalculations() throws Exception {
        // 1. Create ProductEvent
        Events.ProductPayload product = Events.ProductPayload.builder()
                .id(1L)
                .name("Laptop")
                .category("Electronics")
                .price(BigDecimal.valueOf(1000.0))
                .build();
        Events.EventWrapper productEvent = new Events.EventWrapper("ProductCreated", product);
        productTopic.pipeInput("1", objectMapper.writeValueAsString(productEvent));

        // 2. Create OrderEvent
        Events.OrderItem item = Events.OrderItem.builder().productId(1L).quantity(2).price(BigDecimal.valueOf(1000.0)).build();
        Events.OrderPayload order = Events.OrderPayload.builder()
                .id(100L)
                .customerId(10)
                .status("CREATED")
                .items(List.of(item))
                .build();
        Events.EventWrapper orderEvent = new Events.EventWrapper("OrderCreated", order);
        orderTopic.pipeInput("100", objectMapper.writeValueAsString(orderEvent));

        // 3. Verify Product Sales Store (2 * 1000 = 2000.0)
        KeyValueStore<String, Double> productStore = testDriver.getKeyValueStore(AnalyticsTopology.PRODUCT_SALES_STORE);
        Double productSales = productStore.get("1");
        assertThat(productSales).isEqualTo(2000.0);

        // 4. Verify Category Revenue Store (Electronics -> 2000.0)
        KeyValueStore<String, Double> categoryStore = testDriver.getKeyValueStore(AnalyticsTopology.CATEGORY_REVENUE_STORE);
        Double categoryRevenue = categoryStore.get("Electronics");
        assertThat(categoryRevenue).isEqualTo(2000.0);
    }
}
