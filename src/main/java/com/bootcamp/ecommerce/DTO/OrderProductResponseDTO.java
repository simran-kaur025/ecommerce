package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderProductResponseDTO {

    private Long orderProductId;
    private String status;
    private Integer quantity;
    private Double price;
}
