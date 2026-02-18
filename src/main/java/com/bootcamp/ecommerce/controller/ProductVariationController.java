package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.ProductVariationRequestDTO;
import com.bootcamp.ecommerce.DTO.ProductVariationResponse;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.ProductVariationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/product-variations")
@RequiredArgsConstructor
public class ProductVariationController {

    private final ProductVariationService productVariationService;

    @GetMapping("/get/product-variations/{variationId}")
    public ResponseEntity<ResponseDTO> getProductVariation(@PathVariable Long variationId) {

        ProductVariationResponse response = productVariationService.getProductVariation(variationId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }


    @PostMapping("/add/product-variations/{productId}")
    public ResponseEntity<ResponseDTO> addVariation(@PathVariable Long productId, @Valid @RequestBody ProductVariationRequestDTO request) {

        productVariationService.addProductVariation( productId,request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product variation added successfully")
                        .data(null)
                        .build());
    }

    @GetMapping("/get/all/products-variations/{productId}")
    public ResponseEntity<ResponseDTO> getProductVariations(@PathVariable Long productId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query) {

        Page<ProductVariationResponse> response = productVariationService.getAllProductVariations(productId, offset, max, sortBy, order, query);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/variation/{id}")
    public ResponseEntity<ResponseDTO> updateVariation(@PathVariable Long id, @RequestBody ProductVariationRequestDTO request) {

        productVariationService.updateProductVariation(id, request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product variation updated successfully")
                        .data(null)
                        .build()
        );
    }

}
