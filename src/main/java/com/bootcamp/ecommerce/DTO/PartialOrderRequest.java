package com.bootcamp.ecommerce.DTO;

import com.bootcamp.ecommerce.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartialOrderRequest {

    @NotEmpty(message = "product variation ids is required")
    private List<Long> productVariationIds;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be greater than 0")
    private Long addressId;
}