package com.cqrs.command.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(nullable = false)
    private String status;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<OrderItem> items;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
