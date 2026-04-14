package com.cqrs.command.dto;

import com.cqrs.command.domain.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

public class Requests {

    @Getter @Setter
    public static class ProductRequest {
        @NotBlank private String name;
        @NotBlank private String category;
        @NotNull private BigDecimal price;
    }

    @Getter @Setter
    public static class OrderRequest {
        @NotNull private Integer customerId;
        @NotNull private List<OrderItem> items;
    }

    @Getter @Setter
    public static class OrderStatusUpdate {
        @NotBlank private String status;
    }
}
