package com.bootcamp.ecommerce.DTO;


import com.bootcamp.ecommerce.enums.OrderState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull(message = "OrderProductId cannot be null")
    private Long orderProductId;

    @NotBlank(message = "To status cannot be blank")
    private OrderState toStatus;
}