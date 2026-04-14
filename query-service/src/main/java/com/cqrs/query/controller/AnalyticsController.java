package com.cqrs.query.controller;

import com.cqrs.query.dto.Responses;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.cqrs.query.service.AnalyticsTopology.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final StreamsBuilderFactoryBean factoryBean;

    @GetMapping("/products/{productId}/sales")
    public ResponseEntity<Responses.ProductSalesResponse> getProductSales(@PathVariable Long productId) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) return ResponseEntity.status(503).build();

        ReadOnlyKeyValueStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(PRODUCT_SALES_STORE, QueryableStoreTypes.keyValueStore()));

        Double sales = store.get(String.valueOf(productId));
        return ResponseEntity.ok(new Responses.ProductSalesResponse(productId, sales != null ? sales : 0.0));
    }

    @GetMapping("/categories/{categoryName}/revenue")
    public ResponseEntity<Responses.CategoryRevenueResponse> getCategoryRevenue(@PathVariable String categoryName) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) return ResponseEntity.status(503).build();

        ReadOnlyKeyValueStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(CATEGORY_REVENUE_STORE, QueryableStoreTypes.keyValueStore()));

        Double revenue = store.get(categoryName);
        return ResponseEntity.ok(new Responses.CategoryRevenueResponse(categoryName, revenue != null ? revenue : 0.0));
    }

    @GetMapping("/hourly-sales")
    public ResponseEntity<List<Responses.HourlySalesResponse>> getHourlySales() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) return ResponseEntity.status(503).build();

        ReadOnlyWindowStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(HOURLY_SALES_STORE, QueryableStoreTypes.windowStore()));

        // Typically we would ask for start and end times in params. 
        // We will fetch all windows for "GLOBAL_SALES" to satisfy the requirement if params are not strictly enforced,
        // or we can fetch a wide range backwards from now.
        Instant timeTo = Instant.now();
        Instant timeFrom = timeTo.minus(java.time.Duration.ofDays(7));

        org.apache.kafka.streams.state.WindowStoreIterator<Double> iterator = store.fetch("GLOBAL_SALES", timeFrom, timeTo);
        List<Responses.HourlySalesResponse> responses = new ArrayList<>();

        while (iterator.hasNext()) {
            org.apache.kafka.streams.KeyValue<Long, Double> next = iterator.next();
            responses.add(new Responses.HourlySalesResponse(
                    Instant.ofEpochMilli(next.key).toString(),
                    Instant.ofEpochMilli(next.key).plus(java.time.Duration.ofHours(1)).toString(),
                    next.value
            ));
        }
        iterator.close();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/topology")
    public ResponseEntity<String> getTopology() {
        return ResponseEntity.ok(factoryBean.getTopology().describe().toString());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.apache.kafka.streams.errors.InvalidStateStoreException.class)
    public ResponseEntity<String> handleInvalidStateStore() {
        return ResponseEntity.status(503).body("State stores are initializing or rebalancing. Please try again shortly.");
    }
}
