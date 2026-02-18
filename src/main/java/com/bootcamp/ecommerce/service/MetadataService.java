package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;

import java.util.List;

public interface MetadataService {
    CategoryMetadataField createMetadataField(CategoryMetadataFieldRequest request);
    List<CategoryMetadataField> getAllMetadataFields();
}
