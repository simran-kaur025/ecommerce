package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {

    private Long orderId;
    private Double amountPaid;
    private LocalDateTime createdDate;
    private List<OrderProductResponseDTO> products;
}
