package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.ProductVariationService;
import com.bootcamp.ecommerce.utils.RequestParamsExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/product-variation")
@RequiredArgsConstructor
public class ProductVariationController {

    private final ProductVariationService productVariationService;
    private final RequestParamsExtractor extractor;

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{productId}")
    public ResponseEntity<ResponseDTO> addVariation(@PathVariable Long productId, @Valid @RequestBody ProductVariationRequestDTO request) {

        productVariationService.addProductVariation( productId,request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product variation added successfully")
                        .data(null)
                        .build());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{variationId}")
    public ResponseEntity<ResponseDTO> getProductVariation(@PathVariable Long variationId) {

        ProductVariationResponse response = productVariationService.getProductVariation(variationId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/products-variations/{productId}")
    public ResponseEntity<ResponseDTO> getProductVariations(@PathVariable Long productId, @Valid @RequestParam Map<String,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);
        PageResponse<ProductVariationResponse> response = productVariationService.getAllProductVariations(productId,requestParams);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping
    public ResponseEntity<ResponseDTO> updateVariation(@Valid @RequestBody ProductVariationUpdateDTO request) {

        productVariationService.updateProductVariation(request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product variation updated successfully")
                        .data(null)
                        .build()
        );
    }

}
