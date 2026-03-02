package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Locale;

public interface CategoryService {
    ResponseDTO addCategory(CategoryRequestDTO dto, Locale locale);
    CategoryResponse getOneCategory(Long categoryId);
    Page<CategoryResponse> getAllCategories(RequestParams requestParams);
    void addMetadataToCategory( AddCategoryMetadataRequest request);
    void updateMetadataValues(AddCategoryMetadataRequest request);
    void updateCategory(BasicCategoryDTO request);
    List<LeafCategoryResponse> getAllCategoriesAsSeller();
    List<CategoryResponse> getCategoriesAsCustomer(Long categoryId);
    FilterResponse getFilterData(Long categoryId);
}
