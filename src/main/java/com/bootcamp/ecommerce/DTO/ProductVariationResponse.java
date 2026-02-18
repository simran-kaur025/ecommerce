package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductVariationResponse {

    private Long id;
    private Double price;
    private Integer quantityAvailable;
    private boolean active;
    private Map<String, List<String>> metadata;
    private ProductSummary product;
}

