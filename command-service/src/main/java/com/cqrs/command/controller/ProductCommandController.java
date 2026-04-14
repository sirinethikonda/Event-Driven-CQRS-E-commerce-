package com.cqrs.command.controller;

import com.cqrs.command.domain.Product;
import com.cqrs.command.dto.Requests;
import com.cqrs.command.event.EventTypes;
import com.cqrs.command.repository.ProductRepository;
import com.cqrs.command.service.EventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductCommandController {

    private final ProductRepository productRepository;
    private final EventProducer eventProducer;

    @PostMapping
    @Transactional
    public ResponseEntity<Product> createProduct(@RequestBody @Valid Requests.ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
        
        Product saved = productRepository.save(product);
        eventProducer.publishProductCreated(EventTypes.ProductPayload.from(saved));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
