package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

public interface ProductVariationService {
    void addProductVariation(Long productId,ProductVariationRequestDTO request);
    ProductVariationResponse getProductVariation(Long variationId);
    PageResponse<ProductVariationResponse> getAllProductVariations(Long productId,RequestParams params);
    void updateProductVariation(ProductVariationUpdateDTO request);
}
