package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String brand;
    private boolean active;
    private CategoryDetailDTO category;
}

