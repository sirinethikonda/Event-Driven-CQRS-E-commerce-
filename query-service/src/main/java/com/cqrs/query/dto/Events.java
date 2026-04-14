package com.cqrs.query.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

public class Events {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class EventWrapper {
        private String eventType;
        private Object payload; // We can parse into JsonNode or map it later based on eventType
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductPayload {
        private Long id;
        private String name;
        private String category;
        private BigDecimal price;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderPayload {
        private Long id;
        private Integer customerId;
        private String status;
        private List<OrderItem> items;
    }

    // Result DTOs
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EnrichedOrderItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private String category;
    }
}
