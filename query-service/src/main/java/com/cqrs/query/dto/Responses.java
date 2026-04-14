package com.cqrs.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class Responses {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ProductSalesResponse {
        private Long productId;
        private Double totalSales;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CategoryRevenueResponse {
        private String category;
        private Double totalRevenue;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class HourlySalesResponse {
        private String windowStart;
        private String windowEnd;
        private Double totalSales;
    }
}
