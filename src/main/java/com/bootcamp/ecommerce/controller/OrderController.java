package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.OrderResponse;
import com.bootcamp.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/order")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponse> placeOrder() {
        OrderResponse response = orderService.placeOrderForCurrentUser();
        return ResponseEntity.ok(response);
    }

}
