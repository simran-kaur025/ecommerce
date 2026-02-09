package com.bootcamp.ecommerce.DTO;

import java.util.List;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserValidationDTO {
    private String key;
    private List<String> errors;
}

