package com.cqrs.command.controller;

import com.cqrs.command.domain.Order;
import com.cqrs.command.domain.OrderItem;
import com.cqrs.command.dto.Requests;
import com.cqrs.command.repository.OrderRepository;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderCommandController.class)
class OrderCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private EventProducer eventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder() throws Exception {
        Requests.OrderRequest request = new Requests.OrderRequest();
        request.setCustomerId(100);
        request.setItems(List.of(new OrderItem(1L, 2, BigDecimal.valueOf(100))));

        Order order = Order.builder()
                .id(1L)
                .customerId(100)
                .status("CREATED")
                .items(request.getItems())
                .build();

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));

        Mockito.verify(eventProducer).publishOrderCreated(any());
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        Requests.OrderStatusUpdate request = new Requests.OrderStatusUpdate();
        request.setStatus("PAID");

        Order existingOrder = Order.builder()
                .id(1L)
                .customerId(100)
                .status("CREATED")
                .build();

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        Mockito.verify(eventProducer).publishOrderUpdated(1L, "PAID");
    }
}
