package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseDTO {
    private String status;
    private String message;
    private Object data;
}
