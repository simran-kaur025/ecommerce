package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class BasicCategoryDTO {
    private Long id;
    private String name;
}
