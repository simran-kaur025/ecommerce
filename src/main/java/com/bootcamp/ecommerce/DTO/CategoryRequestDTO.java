package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is mandatory")
    private String name;

    private Long parentId;
}

