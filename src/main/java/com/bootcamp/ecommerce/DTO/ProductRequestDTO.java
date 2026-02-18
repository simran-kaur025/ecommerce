package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message = "product name is required")
    private String name;

    @NotBlank(message = "brand name is required")
    private String brand;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull
    @Min(0)
    private Integer quantityAvailable;

    private String description;

    private Boolean isCancellable = false;
    private Boolean isReturnable = false;
}

