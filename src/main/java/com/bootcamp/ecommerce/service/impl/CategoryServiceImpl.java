package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.entity.CategoryMetadataFieldValue;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldRepository;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldValueRepository;
import com.bootcamp.ecommerce.repository.CategoryRepository;
import com.bootcamp.ecommerce.service.CategoryService;
import com.bootcamp.ecommerce.service.CategoryValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryValidationService categoryValidationService;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValueRepository categoryMetadataFieldValueRepository;
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;

    @Override
    @Transactional
    public ResponseDTO addCategory(CategoryRequestDTO dto) {

        List<UserValidationDTO> errors = new ArrayList<>();
        categoryValidationService.validate(dto, errors);

        if (!errors.isEmpty()) {
            return ResponseDTO.builder()
                    .status("FAIL")
                    .message("Validation Failed")
                    .data(errors)
                    .build();
        }

        Category category = new Category();
        category.setName(dto.getName());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository
                    .findById(dto.getParentId())
                    .get();
            category.setParentCategory(parent);
        }

        categoryRepository.save(category);

        return ResponseDTO.builder()
                .status("SUCCESS")
                .message("Category created successfully")
                .data(category.getId())
                .build();
    }

    @Transactional
    @Override
    public  CategoryResponse getOneCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new ResourceNotFoundException("No category found with this id"));

        List<BasicCategoryDTO> parentCategories = getParentCategories(category);
        List<BasicCategoryDTO> childCategories = getChildCategories(category);
        List<MetadataDTO> metaFields = getMetaFields(category);

        return new CategoryResponse(category.getId(), category.getName(), parentCategories, childCategories, metaFields);
    }
    private List<BasicCategoryDTO> getParentCategories(Category category) {
        List<BasicCategoryDTO> parentCategories = new ArrayList<>();
        if (category.getParentCategory() == null) {
            return parentCategories;
        }
        Category currentCategory = category.getParentCategory();
        while (currentCategory != null) {
            parentCategories.add(new BasicCategoryDTO(currentCategory.getId(), currentCategory.getName()));
            currentCategory = currentCategory.getParentCategory();
        }
        Collections.reverse(parentCategories);
        return parentCategories;
    }
    private List<BasicCategoryDTO> getChildCategories(Category category) {
        List<BasicCategoryDTO> childCategories = new ArrayList<>();
        List<Category> children = categoryRepository.findByParentCategory(category);
        for (Category child : children) {
            childCategories.add(new BasicCategoryDTO(child.getId(), child.getName()));
        }
        return childCategories;
    }
    private List<MetadataDTO> getMetaFields(Category category) {

        List<CategoryMetadataFieldValue> metaFieldValues =
                categoryMetadataFieldValueRepository.findByCategory(category);

        return metaFieldValues.stream()
                .map(metaFieldValue -> new MetadataDTO(
                        metaFieldValue.getCategoryMetadataField().getId(),
                        metaFieldValue.getCategoryMetadataField().getName(),
                        metaFieldValue.getValueList()  // Already a List<String>
                ))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(int offset, int max, String sortBy, String order, String query) {

        if (max <= 0) {
            throw new IllegalArgumentException("Max must be greater than 0");
        }

        Sort.Direction direction =
                order.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                offset / max,
                max,
                Sort.by(direction, sortBy)
        );

        Page<Category> categoryPage;

        if (query != null && !query.isBlank()) {
            categoryPage =
                    categoryRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        return categoryPage.map(this::mapToCategoryResponse);
    }

    private CategoryResponse mapToCategoryResponse(Category category) {

        List<BasicCategoryDTO> parentCategories = getParentCategories(category);
        List<BasicCategoryDTO> childCategories = getChildCategories(category);
        List<MetadataDTO> metaFields = getMetaFields(category);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentCategories(parentCategories)
                .childCategories(childCategories)
                .metadata(metaFields)
                .build();
    }


    @Transactional
    @Override
    public void addMetadataToCategory(AddCategoryMetadataRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        for (CategoryMetadataFieldValueRequest dto : request.getFields()) {

            CategoryMetadataField metadataField = categoryMetadataFieldRepository.findById(dto.getMetadataFieldId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Metadata field not found"));

            String valuesString = dto.getValues().trim();

            if (valuesString.isEmpty()) {
                throw new IllegalArgumentException("At least one value must be provided");
            }

            List<String> values = Arrays.stream(valuesString.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .toList();

            if (values.isEmpty()) {
                throw new IllegalArgumentException("Invalid values format");
            }

            Set<String> uniqueValues = new LinkedHashSet<>(values);

            if (uniqueValues.size() != values.size()) {
                throw new IllegalArgumentException("Values must be unique within list");
            }

            boolean exists =
                    categoryMetadataFieldValueRepository.findByCategoryIdAndCategoryMetadataFieldId(
                                    request.getCategoryId(),
                                    dto.getMetadataFieldId()
                            )
                            .isPresent();

            if (exists) {
                throw new IllegalArgumentException(
                        "Metadata field already assigned to this category");
            }

            CategoryMetadataFieldValue entity = new CategoryMetadataFieldValue();
            entity.setCategory(category);
            entity.setCategoryMetadataField(metadataField);
            entity.setValueList(String.join(",", uniqueValues));

            categoryMetadataFieldValueRepository.save(entity);
        }
    }



    @Override
    @Transactional
    public void updateMetadataValues(AddCategoryMetadataRequest request) {

        for (CategoryMetadataFieldValueRequest dto : request.getFields()) {

            CategoryMetadataFieldValue entity =
                    categoryMetadataFieldValueRepository
                            .findByCategoryIdAndCategoryMetadataFieldId(
                                    request.getCategoryId(),
                                    dto.getMetadataFieldId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Metadata not mapped to category"));

            Set<String> existingSet = new HashSet<>();

            if (entity.getValueList() != null && !entity.getValueList().isBlank()) {
                existingSet.addAll(
                        Arrays.stream(entity.getValueList().split(","))
                                .map(String::trim)
                                .collect(Collectors.toSet())
                );
            }

            Set<String> newValues = Arrays.stream(dto.getValues().split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .collect(Collectors.toSet());

            existingSet.addAll(newValues);


            entity.setValueList(String.join(",", existingSet));

            categoryMetadataFieldValueRepository.save(entity);
        }
    }

    @Transactional
    @Override
    public void updateCategory(BasicCategoryDTO request) {

        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String newName = request.getName().trim();

        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (category.getParentCategory() == null) {

            boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(newName, request.getId());

            if (exists) {
                throw new IllegalArgumentException("Category name already exists at root level");
            }

        } else {

            boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentCategoryIdAndIdNot(
                            newName,
                            category.getParentCategory().getId(),
                            request.getId()
                    );

            if (exists) {
                throw new IllegalArgumentException("Category name already exists under same parent");
            }
        }

        category.setName(newName);

        categoryRepository.save(category);
    }

}
