package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponseDTO {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private BasicCategoryDTO category;
    private List<ProductVariationResponse> variations;
}
