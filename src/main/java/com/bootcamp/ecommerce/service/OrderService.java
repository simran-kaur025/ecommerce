package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.OrderResponse;

public interface OrderService {
    OrderResponse placeOrderForCurrentUser();
}
