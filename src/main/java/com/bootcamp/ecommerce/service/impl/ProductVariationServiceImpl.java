package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.ProductSummary;
import com.bootcamp.ecommerce.DTO.ProductVariationRequestDTO;
import com.bootcamp.ecommerce.DTO.ProductVariationResponse;
import com.bootcamp.ecommerce.DTO.ProductVariationUpdateDTO;
import com.bootcamp.ecommerce.entity.CategoryMetadataFieldValue;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.ProductVariation;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldValueRepository;
import com.bootcamp.ecommerce.repository.ProductRepository;
import com.bootcamp.ecommerce.repository.ProductVariationRepository;
import com.bootcamp.ecommerce.service.ProductVariationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariationServiceImpl implements ProductVariationService {

    private final ProductVariationRepository productVariationRepository;
    private final ProductRepository productRepository;
    private final CategoryMetadataFieldValueRepository categoryMetadataFieldValueRepository;


    @Override
    @Transactional
    public void addProductVariation(Long productId,ProductVariationRequestDTO request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getCreatedBy().equals(email)) {
            throw new IllegalArgumentException("Unauthorized product access");
        }

        if (!Boolean.TRUE.equals(product.getIsActive()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Product is not active or deleted");
        }

        if (request.getQuantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (request.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        String image = request.getPrimaryImageName();
        if (image == null || !image.matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Invalid image format");
        }

        Map<String, List<String>> metadata = request.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Variation must have metadata");
        }

        List<CategoryMetadataFieldValue> allowedMeta = categoryMetadataFieldValueRepository.findByCategory(product.getCategory());

        Map<String, Set<String>> allowedMap = allowedMeta.stream()
                .collect(Collectors.toMap(
                        m -> m.getCategoryMetadataField().getName().toLowerCase(),
                        m -> Arrays.stream(m.getValueList().split(","))
                                .map(v -> v.trim().toLowerCase())
                                .collect(Collectors.toSet())
                ));

        for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {

            String field = entry.getKey().trim().toLowerCase();

            if (!allowedMap.containsKey(field)) {
                throw new IllegalArgumentException("Invalid metadata field: " + entry.getKey());
            }

            Set<String> allowedValues = allowedMap.get(field);

            for (String value : entry.getValue()) {

                String normalizedValue = value.trim().toLowerCase();

                if (!allowedValues.contains(normalizedValue)) {
                    throw new IllegalArgumentException(
                            "Invalid value '" + value + "' for field " + entry.getKey());
                }
            }
        }

        List<ProductVariation> existingVariations = productVariationRepository.findByProduct(product);
        if (!existingVariations.isEmpty()) {

            Set<String> expectedKeys = existingVariations.get(0)
                    .getMetadata()
                    .keySet()
                    .stream()
                    .map(k -> k.trim().toLowerCase())
                    .collect(Collectors.toSet());

            Set<String> incomingKeys = metadata.keySet()
                    .stream()
                    .map(k -> k.trim().toLowerCase())
                    .collect(Collectors.toSet());

            if (!expectedKeys.equals(incomingKeys)) {
                throw new IllegalArgumentException(
                        "All variations must have same metadata structure");
            }
        }




        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setQuantity_available(request.getQuantityAvailable());
        variation.setPrice(request.getPrice());
        variation.setPrimaryImageName(image);
        variation.setSecondaryImages(request.getSecondaryImages());
        variation.setMetadata(metadata);
        variation.setIsActive(true);

        productVariationRepository.save(variation);

        log.info("Variation added for product {}", product.getId());
    }



    @Transactional(readOnly = true)
    public ProductVariationResponse getProductVariation(Long variationId) {

        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product variation not found"));

        Product product = variation.getProduct();

        if (product.getIsDeleted()) {
            throw new IllegalArgumentException("Product is deleted");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getSeller().getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not authorized to view this variation");
        }

        return mapToProductVariationResponse(variation);
    }

    private ProductVariationResponse mapToProductVariationResponse(
            ProductVariation variation) {

        Product product = variation.getProduct();

        ProductSummary productSummary = ProductSummary.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .isActive(product.getIsActive())
                .isDeleted(product.getIsDeleted())
                .build();

        return ProductVariationResponse.builder()
                .id(variation.getId())
                .price(variation.getPrice())
                .quantityAvailable(variation.getQuantity_available())
                .active(variation.getIsActive())
                .metadata(variation.getMetadata())
                .product(productSummary)
                .build();
    }



    @Transactional(readOnly = true)
    public Page<ProductVariationResponse> getAllProductVariations(Long productId, int offset, int max, String sortBy, String order, String query) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new IllegalArgumentException("Product is deleted");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getSeller().getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not authorized to view these variations");
        }

        Sort.Direction direction = Sort.Direction.fromOptionalString(order).orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(offset / max, max, Sort.by(direction, sortBy));

        Page<ProductVariation> variationPage;

        if (query != null && !query.isBlank()) {
            variationPage = productVariationRepository.findByProduct_IdAndMetadataContainingIgnoreCase(productId, query, pageable);
        } else {
            variationPage = productVariationRepository.findByProduct_Id(productId, pageable);
        }

        return variationPage.map(this::mapToProductVariationResponse);
    }
//
//    private ProductVariationResponse mapToVariationResponse(
//            ProductVariation variation) {
//
//        return ProductVariationResponse.builder()
//                .id(variation.getId())
//                .price(variation.getPrice())
//                .quantityAvailable(variation.getQuantity_available())
//                .active(variation.getIsActive())
//                .metadata(variation.getMetadata())
//                .product(variation.getProduct())
//                .build();
//    }


    @Transactional
    @Override
    public void updateProductVariation(ProductVariationUpdateDTO request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ProductVariation variation = productVariationRepository.findById(request.getVariationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variation not found"));

        Product product = variation.getProduct();

        if (!product.getCreatedBy().equals(email)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (!product.getIsActive() || product.getIsDeleted()) {
            throw new IllegalArgumentException("Product inactive or deleted");
        }

        if (request.getQuantityAvailable() != null && request.getQuantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }

        if (request.getPrice() != null && request.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }

        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {

            validateMetadata(product, request.getMetadata());
            variation.setMetadata(request.getMetadata());
        }

        if (request.getQuantityAvailable() != null) {
            variation.setQuantity_available(request.getQuantityAvailable());
        }

        if (request.getPrice() != null) {
            variation.setPrice(request.getPrice());
        }

        if (request.getPrimaryImageName() != null) {
            variation.setPrimaryImageName(request.getPrimaryImageName());
        }

        if (request.getSecondaryImages() != null) {
            variation.setSecondaryImages(request.getSecondaryImages());
        }

        if (request.getIsActive() != null) {
            variation.setIsActive(request.getIsActive());
        }

        productVariationRepository.save(variation);
    }



    private void validateMetadata(Product product, Map<String, List<String>> metadata) {

        List<CategoryMetadataFieldValue> allowed =
                categoryMetadataFieldValueRepository
                        .findByCategory(product.getCategory());

        Map<String, Set<String>> allowedMap = new HashMap<>();

        for (CategoryMetadataFieldValue field : allowed) {

            Set<String> values =
                    Arrays.stream(field.getValueList().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());

            allowedMap.put(
                    field.getCategoryMetadataField().getName(),
                    values
            );
        }

        for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {

            String fieldName = entry.getKey();
            List<String> values = entry.getValue();

            if (!allowedMap.containsKey(fieldName)) {
                throw new IllegalArgumentException(
                        "Invalid metadata field: " + fieldName);
            }

            for (String v : values) {
                if (!allowedMap.get(fieldName).contains(v)) {
                    throw new IllegalArgumentException(
                            "Invalid value " + v + " for field " + fieldName);
                }
            }
        }
    }





}
