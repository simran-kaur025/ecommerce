package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CategoryRequestDTO;
import com.bootcamp.ecommerce.DTO.UserValidationDTO;
import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.repository.CategoryRepository;
import com.bootcamp.ecommerce.repository.ProductRepository;
import com.bootcamp.ecommerce.service.CategoryValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryValidationServiceImpl implements CategoryValidationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void validate(CategoryRequestDTO dto, List<UserValidationDTO> errors) {

        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {

            errors.add(
                    UserValidationDTO.builder()
                            .key("Category Name")
                            .errors(List.of("Category name must be unique across tree"))
                            .build()
            );
        }

        if (dto.getParentId() != null) {

            Category parent = categoryRepository.findById(dto.getParentId()).orElse(null);

            if (parent == null) {

                errors.add(
                        UserValidationDTO.builder()
                                .key("Parent Id")
                                .errors(List.of("Parent category not found"))
                                .build()
                );

                return;
            }

            if (productRepository.existsByCategoryId(parent.getId())) {

                errors.add(
                        UserValidationDTO.builder()
                                .key("Parent Id")
                                .errors(List.of("Parent category is already associated with product"))
                                .build()
                );
            }
        }
    }


}

