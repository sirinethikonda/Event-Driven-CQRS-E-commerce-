package com.cqrs.command.event;

import lombok.*;
import com.cqrs.command.domain.Order;
import com.cqrs.command.domain.Product;

public class EventTypes {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EventWrapper<T> {
        private String eventType;
        private T payload;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductPayload {
        private Long id;
        private String name;
        private String category;
        private java.math.BigDecimal price;

        public static ProductPayload from(Product product) {
            return ProductPayload.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderStatusUpdate {
        private Long orderId;
        private String newStatus;
    }
}
