package com.cqrs.command.controller;

import com.cqrs.command.domain.Product;
import com.cqrs.command.dto.Requests;
import com.cqrs.command.repository.ProductRepository;
import com.cqrs.command.service.EventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCommandController.class)
class ProductCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private EventProducer eventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateProduct() throws Exception {
        Requests.ProductRequest request = new Requests.ProductRequest();
        request.setName("Laptop");
        request.setCategory("Electronics");
        request.setPrice(BigDecimal.valueOf(999.99));

        Product product = Product.builder()
                .id(1L)
                .name("Laptop")
                .category("Electronics")
                .price(BigDecimal.valueOf(999.99))
                .build();

        Mockito.when(productRepository.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        Mockito.verify(eventProducer).publishProductCreated(any());
    }
}
