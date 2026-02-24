package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class BasicCategoryDTO {

    @NotNull(message = "Category id is required")
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;
}
