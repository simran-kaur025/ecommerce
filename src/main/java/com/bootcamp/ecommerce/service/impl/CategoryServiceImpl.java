package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.entity.CategoryMetadataFieldValue;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldRepository;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldValueRepository;
import com.bootcamp.ecommerce.repository.CategoryRepository;
import com.bootcamp.ecommerce.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseDTO addCategory(CategoryRequestDTO dto) {
        Category parent = null;

        if (dto.getParentId() != null) {

            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parent category"));

            if (productRepository.existsByCategoryIdAndIsDeletedFalse(parent.getId())) {
                throw new IllegalStateException("Parent category is linked to active products");
            }

            if (!isNameUniqueInBranch(dto.getName(), parent)) {
                throw new IllegalArgumentException("Category name must be unique in this branch");
            }

        } else {

            if (categoryRepository.existsByNameIgnoreCaseAndParentCategoryIsNull(dto.getName())) {
                throw new IllegalArgumentException("Category name already exists at root");
            }
        }

        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setParentCategory(parent);

       categoryRepository.save(category);

        return ResponseDTO.builder()
                .status("SUCCESS")
                .message("Category created successfully")
                .data(category.getId())
                .build();
    }

    private boolean isNameUniqueInBranch(String name, Category parent) {

        if (categoryRepository.existsByNameIgnoreCaseAndParentCategory(name, parent)) {
            return false;
        }

        // Check ancestors
        Category current = parent;
        while (current != null) {
            if (current.getName().equalsIgnoreCase(name)) {
                return false;
            }
            current = current.getParentCategory();
        }

        return !existsInDescendants(parent, name);
    }

    private boolean existsInDescendants(Category category, String name) {

        List<Category> children = categoryRepository.findByParentCategory(category);

        for (Category child : children) {

            if (child.getName().equalsIgnoreCase(name)) {
                return true;
            }

            if (existsInDescendants(child, name)) {
                return true;
            }
        }

        return false;
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
                        metaFieldValue.getValueList()
                ))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(int offset, int max, String sortBy, String order, String query) {

        if (max <= 0) {
            throw new IllegalArgumentException("Max must be greater than 0");
        }

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset / max, max, Sort.by(direction, sortBy));

        Page<Category> categoryPage;

        if (query != null && !query.isBlank()) {
            categoryPage = categoryRepository.findByNameContainingIgnoreCase(query, pageable);
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

            CategoryMetadataFieldValue entity = categoryMetadataFieldValueRepository.findByCategoryIdAndCategoryMetadataFieldId(request.getCategoryId(), dto.getMetadataFieldId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Metadata not mapped to category"));

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

    @Override
    @Transactional
    public void updateCategory(BasicCategoryDTO dto) {

        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category ID"));

        String newName = dto.getName().trim();

        Category parent = category.getParentCategory();

        if (parent != null) {

            if (!isNameUniqueInBranch(newName, parent)) {
                throw new IllegalArgumentException("Category name must be unique in this branch");
            }

        } else {

            boolean exists = categoryRepository
                    .existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(newName, category.getId());

            if (exists) {
                throw new IllegalArgumentException("Category name already exists at root");
            }
        }

        category.setName(newName);

        categoryRepository.save(category);

    }

    @Transactional(readOnly = true)
    @Override
    public List<LeafCategoryResponse> getAllCategoriesAsSeller() {

        List<Category> leaves = categoryRepository.findLeafCategories();

        return leaves.stream()
                .map(category -> LeafCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .parentChain(buildParentChain(category))
                        .metadata(getMetaFields(category))
                        .build())
                .toList();
    }

    private List<String> buildParentChain(Category category) {

        List<String> chain = new ArrayList<>();

        Category current = category.getParentCategory();

        while (current != null) {
            chain.add(current.getName());
            current = current.getParentCategory();
        }

        Collections.reverse(chain);

        return chain;
    }


    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getCategoriesAsCustomer(Long categoryId) {

        List<Category> categories;

        if (categoryId == null) {

            categories = categoryRepository.findByParentCategoryIsNull();

        } else {

            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid categoryId"));

            categories = categoryRepository.findByParentCategory(parent);
        }

        return categories.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private CategoryResponse mapToDTO(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .metadata(getMetaFields(category))
                .build();
    }
}
