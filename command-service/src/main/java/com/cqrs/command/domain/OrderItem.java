package com.cqrs.command.domain;

import lombok.*;
import java.math.BigDecimal;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem implements Serializable {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
