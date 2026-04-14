package com.cqrs.command.controller;

import com.cqrs.command.domain.Order;
import com.cqrs.command.dto.Requests;
import com.cqrs.command.repository.OrderRepository;
import com.cqrs.command.service.EventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderRepository orderRepository;
    private final EventProducer eventProducer;

    @PostMapping
    @Transactional
    public ResponseEntity<Order> createOrder(@RequestBody @Valid Requests.OrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .items(request.getItems())
                .status("CREATED")
                .build();
        
        Order saved = orderRepository.save(order);
        eventProducer.publishOrderCreated(saved);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody @Valid Requests.OrderStatusUpdate request) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(request.getStatus());
        Order saved = orderRepository.save(order);
        
        eventProducer.publishOrderUpdated(saved.getId(), saved.getStatus());
        
        return ResponseEntity.ok(saved);
    }
}
