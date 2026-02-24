package com.bootcamp.ecommerce.DTO;

import com.bootcamp.ecommerce.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

    @NotNull
    private Long productVariationId;

    @NotNull
    @Min(1)
    private Integer quantity;


    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be greater than 0")
    private Long addressId;
}

