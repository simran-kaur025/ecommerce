package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldRepository;
import com.bootcamp.ecommerce.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;

    @Override
    public CategoryMetadataField createMetadataField(CategoryMetadataFieldRequest request) {

        if (categoryMetadataFieldRepository.existsByName(request.getName())) {
            throw new BadRequestException("Metadata field name must be unique");
        }

        CategoryMetadataField field = new CategoryMetadataField();
        field.setName(request.getName().trim());

        return categoryMetadataFieldRepository.save(field);
    }

    @Override
    public List<CategoryMetadataField> getAllMetadataFields() {
       return categoryMetadataFieldRepository.findAll();
    }
}
