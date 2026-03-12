package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

public interface ProductService {
    void addProduct(ProductRequestDTO request);
    ProductResponse getProduct(Long productId);
    PageResponse<ProductResponse> getAllProducts(RequestParams params);
    void deleteProduct(Long productId);
    void updateProduct(Long productId, UpdateProductRequest request);


    ProductDetailResponseDTO viewProductAsCustomer(Long productId);
    PageResponse<ProductDetailResponseDTO> getAllProductsAsCustomer(Long categoryId,RequestParams requestParams);
    ProductListResponseDTO getSimilarProducts(Long productId,RequestParams params);


    ProductDetailResponseDTO viewProductAsAdmin(Long productId);
    PageResponse<ProductDetailResponseDTO>viewAllProductsAsAdmin(RequestParams params);
    void deactivateProduct(Long productId);
    void activateProduct(Long productId);
}
