package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.DTO.MetadataPageResponse;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

public interface MetadataService {
    CategoryMetadataField createMetadataField(CategoryMetadataFieldRequest request);

    MetadataPageResponse getAllMetadataFields(int offset, int max, String sortBy, String order, String query);
}
