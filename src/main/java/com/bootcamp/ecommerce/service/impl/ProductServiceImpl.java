package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.entity.*;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.exceptionalHandler.ProductInactiveException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.*;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.ProductService;
import com.bootcamp.ecommerce.specifications.ProductSpecifications;
import com.bootcamp.ecommerce.specifications.ProductVariationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


import static com.bootcamp.ecommerce.constant.EmailConstants.ADMIN_EMAIL;

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
        log.info("Add product request received from seller email: {}", email);

        Seller seller = sellerRepository.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Seller not Found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not Found"));

        if (categoryRepository.existsByParentCategoryId(category.getId())) {
            throw new BadRequestException("Product can only be added to leaf category");
        }

        boolean exists = productRepository.existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategoryAndSeller(request.getName(), request.getBrand(), category, seller);

        if (exists) {
            log.warn("Duplicate product attempt: name={}, brand={}, seller={}", request.getName(), request.getBrand(), seller.getId());

            throw new BadRequestException("Product already exists for this seller, brand and category");
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
        log.info("Product saved successfully with id {} by seller {}", product.getId(), seller.getId());

        emailService.sendProductApprovalEmail(ADMIN_EMAIL, product, seller.getCompanyName());

    }


        @Cacheable(
                value = "products",
                key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"
        )
        @Override
        @Transactional(readOnly = true)
        public ProductResponse getProduct(Long productId) {
            log.info("Fetching product with id {}", productId);

            Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getIsDeleted()) {
                throw new ResourceNotFoundException("Product is deleted");
            }

            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            if (!product.getCreatedBy().equals(email)) {
                throw new AccessDeniedException("You are not authorized to update this product");
            }
            log.info("Product {} fetched successfully by user {}", productId, email);
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



    @Cacheable(value = "productsList", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name + '_' + #requestParams.cacheKey()")
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(RequestParams requestParams) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching product list for seller {}", email);


        Map<String, String> filters = new HashMap<>(requestParams.getFilters());
        filters.put("sellerEmail", email);

        Specification<Product> specification = ProductSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction.fromOptionalString(requestParams.getOrder())
                .orElse(Sort.Direction.ASC);

        Sort sort = Sort.by(direction, requestParams.getSortBy());

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(pageNumber, requestParams.getMax(), sort);

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        log.info("Fetched {} products for seller {}", productPage.getTotalElements(), email);
        List<ProductResponse> products = productPage
                .getContent()
                .stream()
                .map(this::mapToProductResponse)
                .toList();

        return new PageResponse<>(
                products,
                productPage.getTotalElements(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages()
        );
    }


    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"),
            @CacheEvict(value = "productsList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "publicProductDetail", allEntries = true),
            @CacheEvict(value = "publicProductList", allEntries = true)
    })
    @Transactional
    @Override
    public void deleteProduct(Long productId) {

        log.info("Delete product request received for productId {}", productId);

        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new ProductInactiveException("Product already deleted");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getCreatedBy().equals(email)) {
            throw new AccessDeniedException("You are not authorized to update this product");
        }

        product.setIsDeleted(true);

        productRepository.save(product);
        log.info("Product {} successfully deleted by user {}", productId, email);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name"),
            @CacheEvict(value = "productsList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "publicProductDetail", allEntries = true),
            @CacheEvict(value = "publicProductList", allEntries = true)
    })
    @Transactional
    @Override
    public void updateProduct(Long productId, UpdateProductRequest request) {

        log.info("Update product request received for productId {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted()) {
            throw new ProductInactiveException("Product is deleted or not active");
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
                throw new BadRequestException("Product name already exists for this brand and category");
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
        log.info("Product {} updated successfully by user {}", productId, email);
    }


    @Cacheable(value = "publicProductDetail", key = "#productId")
    @Transactional(readOnly = true)
    @Override
    public ProductDetailResponseDTO viewProductAsCustomer(Long productId) {
        log.info("Customer request received to view product {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsDeleted() || !product.getIsActive()) {
            throw new ProductInactiveException("Product not available");
        }

        List<ProductVariation> variations = productVariationRepository.findByProductAndIsActiveTrue(product);

        if (variations.isEmpty()) {
            throw new BadRequestException("Product has no valid variations");
        }
        log.info("Product {} fetched successfully with {} variations", productId, variations.size());

        return mapToProductDetailResponse(product, variations);
    }



    @Cacheable(
            value = "publicProductList",
            key = "#categoryId + '_' + #requestParams.cacheKey()"
    )
    @Transactional(readOnly = true)
    @Override
    public PageResponse<ProductDetailResponseDTO> getAllProductsAsCustomer(Long categoryId, RequestParams requestParams) {

        log.info("Customer requested product list for category {}", categoryId);
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));

        List<Long> leafCategoryIds = getLeafCategoryIds(category.getId());

        Map<String, String> filters = new HashMap<>(requestParams.getFilters());

        filters.put("categoryIds", leafCategoryIds.toString());

        Specification<Product> specification = ProductSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction.fromOptionalString(requestParams.getOrder()).orElse(Sort.Direction.ASC);

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(pageNumber, requestParams.getMax(), Sort.by(direction, requestParams.getSortBy()));

        Page<Product> productPage = productRepository.findAll(specification, pageable);
        log.info("Fetched {} products for category {}", productPage.getTotalElements(), categoryId);

        List<ProductDetailResponseDTO> products = productPage.getContent()
                .stream()
                .map(p -> {
                    Specification<ProductVariation> variationSpec = ProductVariationSpecifications.extract(p, requestParams.getFilters());

                    List<ProductVariation> variations = productVariationRepository.findAll(variationSpec);

                    return mapToProductDetailResponse(p, variations);
                })
                .toList();
        log.info("Returning paginated product response for category {}", categoryId);

        return new PageResponse<>(
                products,
                productPage.getTotalElements(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages()
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
                .metadata(variation.getMetadata())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductListResponseDTO getSimilarProducts(Long productId, RequestParams params) {

        log.info("Request received to fetch similar products for productId {}", productId);

        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Map<String, String> filters = new HashMap<>();

        filters.put("categoryId", product.getCategory().getId().toString());

        Specification<Product> specification = ProductSpecifications.extract(filters)
                        .and((root, query, cb) ->
                                cb.notEqual(root.get("id"), productId));

        Sort.Direction direction = Sort.Direction.fromOptionalString(params.getOrder()).orElse(Sort.Direction.ASC);

        int pageNumber = params.getOffset() / params.getMax();

        Pageable pageable = PageRequest.of(pageNumber, params.getMax(), Sort.by(direction, params.getSortBy()));

        Page<Product> page = productRepository.findAll(specification, pageable);
        log.info("Found {} similar products for product {}", page.getTotalElements(), productId);
        List<ProductDetailResponseDTO> products = page.getContent()
                        .stream()
                        .map(p -> {
                                Specification<ProductVariation> variationSpec = ProductVariationSpecifications.extract(p, params.getFilters());
                                List<ProductVariation> variations = productVariationRepository.findAll(variationSpec);
                                return mapToProductDetailResponse(p, variations);

                        })
                        .toList();
        log.info("Returning similar product response for product {}", productId);

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

        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        log.info("Product found: {}", product.getName());

        List<ProductVariation> variations = productVariationRepository.findByProduct(product);
        log.info("Total variations found for productId {} : {}", productId, variations.size());
        return mapToProductDetailResponse(product, variations);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<ProductDetailResponseDTO> viewAllProductsAsAdmin(RequestParams requestParams) {

        Map<String, String> filters = new HashMap<>(requestParams.getFilters());

        Specification<Product> specification = ProductSpecifications.extract(filters);

        Sort.Direction direction = Sort.Direction.fromOptionalString(requestParams.getOrder())
                        .orElse(Sort.Direction.ASC);

        int pageNumber = requestParams.getOffset() / requestParams.getMax();

        Pageable pageable = PageRequest.of(pageNumber, requestParams.getMax(), Sort.by(direction, requestParams.getSortBy()));

        Page<Product> productPage = productRepository.findAll(specification, pageable);
        log.info("Total products found: {}", productPage.getTotalElements());

        List<ProductDetailResponseDTO> products = productPage.getContent()
                        .stream()
                        .map(product -> {

                            Specification<ProductVariation> variationSpec = ProductVariationSpecifications.extract(product, requestParams.getFilters());

                            List<ProductVariation> variations = productVariationRepository.findAll(variationSpec);
                            log.debug("Total variations found for productId {} : {}", product.getId(), variations.size());
                            return mapToProductDetailResponse(product, variations);
                        })
                        .toList();
        log.info("Returning {} products for admin request", products.size());

        return new PageResponse<>(
                products,
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
        );
    }


    @Transactional
    @Override
    public void deactivateProduct(Long productId) {

        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product already inactive");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is deleted");
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
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product already active");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Cannot activate deleted product");
        }

        product.setIsActive(true);

        productRepository.save(product);

         emailService.sendProductActivatedEmail(product.getCreatedBy(),product);

        log.info("Product {} activated", productId);
    }








}
