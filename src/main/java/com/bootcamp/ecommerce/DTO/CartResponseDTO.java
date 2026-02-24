package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartResponseDTO {

    private Long productVariationId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Boolean isOutOfStock;
}
