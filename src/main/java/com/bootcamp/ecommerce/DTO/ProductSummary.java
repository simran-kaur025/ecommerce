package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSummary {

    private Long id;
    private String name;
    private String brand;
    private boolean isActive;
    private boolean isDeleted;
}
