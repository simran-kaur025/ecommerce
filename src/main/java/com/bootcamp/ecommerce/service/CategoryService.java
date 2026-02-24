package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    ResponseDTO addCategory(CategoryRequestDTO dto);
    CategoryResponse getOneCategory(Long categoryId);
    Page<CategoryResponse> getAllCategories(int offset, int max, String sortBy, String order, String query);
    void addMetadataToCategory( AddCategoryMetadataRequest request);
    void updateMetadataValues(AddCategoryMetadataRequest request);
    void updateCategory(BasicCategoryDTO request);
    List<LeafCategoryResponse> getAllCategoriesAsSeller();
    List<CategoryResponse> getCategoriesAsCustomer(Long categoryId);
}
