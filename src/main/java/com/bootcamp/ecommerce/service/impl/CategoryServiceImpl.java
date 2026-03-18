package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.entity.CategoryMetadataFieldValue;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.CategoryService;
import com.bootcamp.ecommerce.service.CategoryValidationService;
import com.bootcamp.ecommerce.specifications.CategorySpecification;
import com.bootcamp.ecommerce.specifications.MetadataFieldSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

import static com.bootcamp.ecommerce.specifications.MetadataFieldSpecification.filter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryValidationService categoryValidationService;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValueRepository categoryMetadataFieldValueRepository;
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    private final ProductRepository productRepository;
    private final MessageSource messageSource;
    private final ProductVariationRepository productVariationRepository;


    @Override
    @Transactional
    public ResponseDTO addCategory(CategoryRequestDTO dto, Locale locale) {
        Category parent = null;

        if (dto.getParentId() != null) {

            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> {
                        log.error("Parent category not found with id {}", dto.getParentId());
                        return new ResourceNotFoundException("Invalid parent category");
                    });

            if (productRepository.existsByCategoryIdAndIsDeletedFalse(parent.getId())) {
                throw new BadRequestException("Parent category is linked to active products");
            }

            if (!isNameUniqueInBranch(dto.getName(), parent)) {
                throw new BadRequestException("Category name must be unique in this branch");
            }

        } else {

            if (categoryRepository.existsByNameIgnoreCaseAndParentCategoryIsNull(dto.getName())) {
                throw new BadRequestException("Category name already exists at root");
            }
        }

        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setParentCategory(parent);

       categoryRepository.save(category);
        log.info("Category {} created successfully with id {}", dto.getName(), category.getId());

        String msg=messageSource.getMessage("category.created.success",null,locale);
        return ResponseDTO.builder()
                .status("SUCCESS")
                .message(msg)
                .data(category.getId())
                .build();
    }

    private boolean isNameUniqueInBranch(String name, Category parent) {

        if (categoryRepository.existsByNameIgnoreCaseAndParentCategory(name, parent)) {
            return false;
        }
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

        log.info("Request received to fetch category with id {}", categoryId);

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                        new ResourceNotFoundException("No category found with this id"));

        List<BasicCategoryDTO> parentCategories = getParentCategories(category);
        List<BasicCategoryDTO> childCategories = getChildCategories(category);
        List<MetadataDTO> metaFields = getMetaFields(category);

        log.info("Category {} fetched successfully", categoryId);

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
    public Page<CategoryResponse> getAllCategories(RequestParams params) {

        log.info("Fetching category list with params: {}", params);

        Specification<Category> specification = CategorySpecification.filter(params.getFilters());

        Sort.Direction direction = Sort.Direction.fromOptionalString(params.getOrder())
                        .orElse(Sort.Direction.ASC);

        Sort sort = Sort.by(direction, params.getSortBy());

        int pageNumber = params.getOffset() / params.getMax();

        Pageable pageable = PageRequest.of(pageNumber, params.getMax(), sort);
        log.debug("Pagination applied - pageNumber: {}, pageSize: {}, sort: {} {}", pageNumber, params.getMax(), params.getSortBy(), direction);

        Page<Category> categoryPage = categoryRepository.findAll(specification, pageable);

        log.info("Fetched {} categories", categoryPage.getTotalElements());

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
        log.info("Request received to add metadata to category {}", request.getCategoryId());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        for (CategoryMetadataFieldValueRequest dto : request.getFields()) {

            CategoryMetadataField metadataField = categoryMetadataFieldRepository.findById(dto.getMetadataFieldId())
                            .orElseThrow(() -> new ResourceNotFoundException("Metadata field not found"));

            String valuesString = dto.getValues().trim();

            if (valuesString.isEmpty()) {
                throw new BadRequestException("At least one value must be provided");
            }

            List<String> values = Arrays.stream(valuesString.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .toList();

            if (values.isEmpty()) {
                throw new BadRequestException("Invalid values format");
            }

            Set<String> uniqueValues = new LinkedHashSet<>(values);

            if (uniqueValues.size() != values.size()) {
                log.warn("Duplicate values detected in metadata field {}", dto.getMetadataFieldId());
                throw new BadRequestException("Values must be unique within list");
            }

            boolean exists = categoryMetadataFieldValueRepository.findByCategoryIdAndCategoryMetadataFieldId(
                                    request.getCategoryId(),
                                    dto.getMetadataFieldId()
                            )
                            .isPresent();

            if (exists) {
                log.warn("Metadata field {} already assigned to category {}", dto.getMetadataFieldId(), request.getCategoryId());

                throw new BadRequestException("Metadata field already assigned to this category");
            }

            CategoryMetadataFieldValue entity = new CategoryMetadataFieldValue();
            entity.setCategory(category);
            entity.setCategoryMetadataField(metadataField);
            entity.setValueList(String.join(",", uniqueValues));

            categoryMetadataFieldValueRepository.save(entity);
            log.info("Metadata field {} added to category {} with values {}",   dto.getMetadataFieldId(), request.getCategoryId(), uniqueValues);
        }
    }



    @Override
    @Transactional
    public void updateMetadataValues(AddCategoryMetadataRequest request) {

        log.info("Request received to update metadata values for category {}", request.getCategoryId());

        for (CategoryMetadataFieldValueRequest dto : request.getFields()) {
            log.debug("Updating metadata field {} for category {}", dto.getMetadataFieldId(), request.getCategoryId());

            CategoryMetadataFieldValue entity = categoryMetadataFieldValueRepository.findByCategoryIdAndCategoryMetadataFieldId(request.getCategoryId(), dto.getMetadataFieldId())
                            .orElseThrow(() -> new ResourceNotFoundException("Metadata not mapped to category"));

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
            log.info("Metadata field {} updated for category {} with values {}", dto.getMetadataFieldId(), request.getCategoryId(), existingSet);
        }
    }

    @Override
    @Transactional
    public void updateCategory(BasicCategoryDTO dto) {
        log.info("Request received to update category {}", dto.getId());
        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category ID"));

        String newName = dto.getName().trim();
        log.debug("Updating category {} with new name '{}'", dto.getId(), newName);

        Category parent = category.getParentCategory();

        if (parent != null) {

            if (!isNameUniqueInBranch(newName, parent)) {
                log.warn("Duplicate category name '{}' in branch {}", newName, parent.getId());
                throw new BadRequestException("Category name must be unique in this branch");
            }

        } else {
            boolean exists = categoryRepository .existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(newName, category.getId());

            if (exists) {
                log.warn("Duplicate root category name '{}'", newName);
                throw new BadRequestException("Category name already exists at root");
            }
        }

        category.setName(newName);

        categoryRepository.save(category);
        log.info("Category {} updated successfully with new name '{}'", dto.getId(), newName);

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
        log.info("Customer requested categories for parentId {}", categoryId);

        List<Category> categories;

        if (categoryId == null) {
            categories = categoryRepository.findByParentCategoryIsNull();
        } else {
            log.debug("Fetching child categories for parentId {}", categoryId);
            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid categoryId"));

            categories = categoryRepository.findByParentCategory(parent);
        }

        log.info("Fetched {} categories for parentId {}", categories.size(), categoryId);
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

    @Override
    public FilterResponse getFilterData(Long categoryId) {

        log.info("Request received to fetch filter data for category {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Category> categories = getAllDescendantCategories(category);

        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        log.debug("Total descendant categories found: {}", categoryIds.size());

        Map<String, List<String>> metadataFields = new HashMap<>();

        List<CategoryMetadataFieldValue> values = categoryMetadataFieldValueRepository.findByCategoryIds(categoryIds);

        log.debug("Metadata field values fetched: {}", values.size());

        for (CategoryMetadataFieldValue value : values) {

            String fieldName = value.getCategoryMetadataField().getName();
            String fieldValue = value.getValueList();
            metadataFields.putIfAbsent(fieldName, new ArrayList<>());

            if (!metadataFields.get(fieldName).contains(fieldValue)) {
                metadataFields.get(fieldName).add(fieldValue);
            }
        }

        List<String> brands = productRepository.findDistinctBrandByCategoryIds(categoryIds);

        Object[] result = productVariationRepository.findMinMaxPriceByCategories(categoryIds);

        Double minPrice = 0.0;
        Double maxPrice = 0.0;

        if (result != null && result.length > 0) {

            Object[] priceRange = (Object[]) result[0];

            if (priceRange[0] != null) {
                minPrice = ((Number) priceRange[0]).doubleValue();
            }

            if (priceRange[1] != null) {
                maxPrice = ((Number) priceRange[1]).doubleValue();
            }
        }

        log.info("Filter data prepared for category {} -> brands: {}, minPrice: {}, maxPrice: {}", categoryId, brands.size(), minPrice, maxPrice);
        return new FilterResponse(metadataFields, brands, minPrice, maxPrice);
    }

    private List<Category> getAllDescendantCategories(Category category) {

        List<Category> result = new ArrayList<>();
        result.add(category);

        List<Category> children = categoryRepository.findByParentCategory(category);

        for (Category child : children) {
            result.addAll(getAllDescendantCategories(child));
        }

        return result;
    }
}
