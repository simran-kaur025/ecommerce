package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.ProductVariationRequestDTO;
import com.bootcamp.ecommerce.DTO.ProductVariationResponse;
import com.bootcamp.ecommerce.DTO.ProductVariationUpdateDTO;
import org.springframework.data.domain.Page;

public interface ProductVariationService {
    void addProductVariation(Long productId,ProductVariationRequestDTO request);
    ProductVariationResponse getProductVariation(Long variationId);
    Page<ProductVariationResponse> getAllProductVariations(Long productId, int offset, int max, String sortBy, String order, String query);
    void updateProductVariation(ProductVariationUpdateDTO request);
}
