package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

public interface ProductService {
    void addProduct(ProductRequestDTO request);
    ProductResponse getProduct(Long productId);
    Page<ProductResponse> getAllProducts(int offset, int max, String sortBy, String order, String query);
    void deleteProduct(Long productId);
    void updateProduct(Long productId, UpdateProductRequest request);


    ProductDetailResponseDTO viewProductAsCustomer(Long productId);
    ProductListResponseDTO getAllProductsAsCustomer(
            Long categoryId,
            int offset,
            int max,
            String sort,
            String order,
            String query);

    ProductListResponseDTO getSimilarProducts(
            Long productId,
            String query,
            int offset,
            int max,
            String sort,
            String order);


    ProductDetailResponseDTO viewProductAsAdmin(Long productId);
    ProductListResponseDTO viewAllProductsAsAdmin(
            int offset,
            int max,
            String sort,
            String order,
            Long categoryId,
            Long sellerId,
            String query
    );
    void deactivateProduct(Long productId);
    void activateProduct(Long productId);
}
