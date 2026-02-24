package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.*;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bootcamp.ecommerce.constant.Constant.ADMIN_EMAIL;
import static com.bootcamp.ecommerce.constant.Constant.PRODUCT_APPROVAL_SUBJECT;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CategoryMetadataFieldValueRepository categoryMetadataFieldValueRepository;
    private final EmailService emailService;


    @CacheEvict(value = {"productList"}, allEntries = true)
    @Override
    public void addProduct(ProductRequestDTO request) {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();

        Seller seller = sellerRepository.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Seller not Found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not Found"));

        if (categoryRepository.existsByParentCategoryId(category.getId())) {
            throw new IllegalArgumentException("Product can only be added to leaf category");
        }

        boolean exists = productRepository.existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategoryAndSeller(request.getName(), request.getBrand(), category, seller);

        if (exists) {
            throw new IllegalArgumentException("Product already exists for this seller, brand and category");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setIsCancellable(request.getIsCancellable());
        product.setIsReturnable(request.getIsReturnable());
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsActive(false);

        productRepository.save(product);

        emailService.sendProductApprovalEmail(ADMIN_EMAIL, product, seller.getCompanyName());

        log.info("Product created successfully by seller {}", seller.getId());


    }


    @Cacheable(
            value = "products",
            key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"
    )
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {

        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new IllegalArgumentException("Product is deleted");
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (!product.getCreatedBy().equals(email)) {
            throw new AccessDeniedException("You are not authorized to update this product");
        }

        return mapToProductResponse(product);
    }

    private ProductResponse mapToProductResponse(Product product) {

        Category category = product.getCategory();

        BasicCategoryDTO parent = null;
        if (category.getParentCategory() != null) {
            parent = new BasicCategoryDTO(
                    category.getParentCategory().getId(),
                    category.getParentCategory().getName()
            );
        }

        List<MetadataDTO> metadata = categoryMetadataFieldValueRepository.findByCategory(category)
                        .stream()
                        .map(meta -> new MetadataDTO(
                                meta.getCategoryMetadataField().getId(),
                                meta.getCategoryMetadataField().getName(),
                                meta.getValueList()
                        ))
                        .toList();

        CategoryDetailDTO categoryDTO = CategoryDetailDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentCategory(parent)
                .metadata(metadata)
                .build();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .active(product.getIsActive())
                .category(categoryDTO)
                .build();
    }


    @Cacheable(
            value = "productsList",
            key = "#offset + '_' + #max + '_' + #sortBy + '_' + #order + '_' + #query + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"
    )
    @Transactional(readOnly = true)
    @Override
    public PageResponse<ProductResponse> getAllProducts(int offset, int max, String sortBy, String order, String query) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Sort.Direction direction = Sort.Direction.fromOptionalString(order).orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(offset / max, max, Sort.by(direction, sortBy));

        Page<Product> productPage;

        if (query != null && !query.isBlank()) {
            productPage = productRepository.findBySeller_User_EmailAndIsDeletedFalseAndNameContainingIgnoreCase(email, query, pageable);
        } else {
            productPage = productRepository.findBySeller_User_EmailAndIsDeletedFalse(email, pageable);
        }

        PageResponse<ProductResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(productPage.map(this::mapToProductResponse).getContent());
        pageResponse.setTotalElements(productPage.getTotalElements());
        pageResponse.setTotalPages(productPage.getTotalPages());
        pageResponse.setNumber(productPage.getNumber());
        pageResponse.setSize(productPage.getSize());

        return pageResponse;
    }


    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"),
            @CacheEvict(value = "productsList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "publicProductDetail", allEntries = true)
    })
    @Transactional
    @Override
    public void deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new IllegalArgumentException("Product already deleted");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getCreatedBy().equals(email)) {
            throw new AccessDeniedException("You are not authorized to update this product");
        }

        product.setIsDeleted(true);

        productRepository.save(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"),
            @CacheEvict(value = "productsList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "publicProductDetail", allEntries = true)
    })
    @Transactional
    @Override
    public void updateProduct(Long productId, UpdateProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new IllegalArgumentException("Product is deleted or not active");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getCreatedBy().equals(email)) {
            throw new AccessDeniedException("You are not authorized to update this product");
        }


        if (request.getName() != null && !request.getName().equalsIgnoreCase(product.getName())) {

            boolean exists = productRepository
                            .existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategory_IdAndSeller_Id(
                                    request.getName(),
                                    product.getBrand(),
                                    product.getCategory().getId(),
                                    product.getSeller().getId()
                            );

            if (exists) {
                throw new IllegalArgumentException(
                        "Product name already exists for this brand and category");
            }

            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getIsCancellable() != null) {
            product.setIsCancellable(request.getIsCancellable());
        }

        if (request.getIsReturnable() != null) {
            product.setIsReturnable(request.getIsReturnable());
        }

        productRepository.save(product);
    }


    @Cacheable(value = "publicProductDetail", key = "#productId")
    @Transactional(readOnly = true)
    @Override
    public ProductDetailResponseDTO viewProductAsCustomer(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted() || !product.getIsActive()) {
            throw new IllegalArgumentException("Product not available");
        }

        List<ProductVariation> variations = productVariationRepository.findByProductAndIsActiveTrue(product);

        if (variations.isEmpty()) {
            throw new IllegalArgumentException("Product has no valid variations");
        }

        return mapToProductDetailResponse(product, variations);
    }

    @Cacheable(
            value = "publicProductList",
            key = "#categoryId + '_' + #offset + '_' + #max + '_' + #sort + '_' + #order + '_' + #query"
    )
    @Transactional(readOnly = true)
    @Override
    public PageResponse<ProductDetailResponseDTO> getAllProductsAsCustomer(
            Long categoryId,
            int offset,
            int max,
            String sort,
            String order,
            String query) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Long> leafCategoryIds = getLeafCategoryIds(category.getId());

        Sort.Direction direction =
                order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Product> page = productRepository.searchProductsWithCategory(leafCategoryIds, query, pageable);

        List<ProductDetailResponseDTO> products =
                page.getContent()
                        .stream()
                        .map(product -> {

                            List<ProductVariation> variations =
                                    productVariationRepository.findByProduct(product);

                            return mapToProductDetailResponse(product, variations);
                        })
                        .toList();

        return new PageResponse<ProductDetailResponseDTO>(
                products,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }



    private List<Long> getLeafCategoryIds(Long categoryId) {

        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);

        if (children.isEmpty()) {
            return List.of(categoryId);
        }

        List<Long> leafIds = new ArrayList<>();

        for (Category child : children) {
            leafIds.addAll(getLeafCategoryIds(child.getId()));
        }

        return leafIds;
    }



    private ProductDetailResponseDTO mapToProductDetailResponse(Product product, List<ProductVariation> variations) {

        Category category = product.getCategory();

        BasicCategoryDTO categorySummary = BasicCategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build();

        List<ProductVariationResponse> variationResponses =
                variations.stream()
                        .map(this::mapToVariationResponse)
                        .toList();

        return ProductDetailResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .category(categorySummary)
                .variations(variationResponses)
                .build();
    }

    private ProductVariationResponse mapToVariationResponse(ProductVariation variation) {

        return ProductVariationResponse.builder()
                .id(variation.getId())
                .price(variation.getPrice())
                .quantityAvailable(variation.getQuantity_available())
//                .primaryImage(variation.getPrimaryImageName())
//                .secondaryImages(variation.getSecondaryImages())
                .metadata(variation.getMetadata())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductListResponseDTO getSimilarProducts(
            Long productId,
            String query,
            int offset,
            int max,
            String sort,
            String order) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Sort.Direction direction =
                order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Product> page = productRepository.findSimilarProducts(product.getCategory().getId(), product.getId(), query, pageable
        );

        List<ProductDetailResponseDTO> products =
                page.getContent()
                        .stream()
                        .map(p -> mapToProductDetailResponse(p, productVariationRepository.findByProduct(p)))
                        .toList();

        return ProductListResponseDTO.builder()
                .products(products)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }




    @Transactional(readOnly = true)
    @Override
    public ProductDetailResponseDTO viewProductAsAdmin(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<ProductVariation> variations = productVariationRepository.findByProduct(product);

        return mapToProductDetailResponse(product, variations);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductListResponseDTO viewAllProductsAsAdmin(
            int offset,
            int max,
            String sort,
            String order,
            Long categoryId,
            Long sellerId,
            String query
    ) {

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Product> productPage = productRepository.searchProducts(categoryId, sellerId, query, pageable);

        List<ProductDetailResponseDTO> products = productPage.getContent()
                .stream()
                .map(product -> {

                    List<ProductVariation> variations = productVariationRepository.findByProduct(product);

                    return mapToProductDetailResponse(product, variations);
                })
                .toList();

        return ProductListResponseDTO.builder()
                .products(products)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }






    @Transactional
    @Override
    public void deactivateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product already inactive");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Product is deleted");
        }

        product.setIsActive(false);

        productRepository.save(product);

        emailService.sendProductDeactivatedEmail(product.getCreatedBy(),product);

        log.info("Product {} deactivated", productId);
    }



    @Transactional
    @Override
    public void activateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product already active");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Cannot activate deleted product");
        }

        product.setIsActive(true);

        productRepository.save(product);

         emailService.sendProductActivatedEmail(product.getCreatedBy(),product);

        log.info("Product {} activated", productId);
    }








}
