package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDetailResponseDTO {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private BasicCategoryDTO category;
    private List<ProductVariationResponse> variations;
}
