package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryMetadataFieldRequest {
    @NotBlank(message = "name is required")
    private String name;
}
