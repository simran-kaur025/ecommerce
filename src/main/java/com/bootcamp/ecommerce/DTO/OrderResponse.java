package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {

    private String message;
    private Long orderId;
}

