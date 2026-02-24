package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    @NotNull(message = "product variation id is required")
    private Long productVariationId;

    @NotNull(message = "quantity is required")
    @Min(0)
    private Integer quantity;
}
