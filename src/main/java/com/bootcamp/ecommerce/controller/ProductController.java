package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.ProductService;
import com.bootcamp.ecommerce.utils.RequestParamsExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RequestParamsExtractor extractor;


    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<ResponseDTO> addProduct(@Valid @RequestBody ProductRequestDTO request) {

        productService.addProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(Constant.SUCCESS, "Product created successfully",null));
    }



    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ResponseDTO> deleteProduct(@PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Product deleted successfully")
                        .build()
        );
    }


    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{productId}")
    public ResponseEntity<ResponseDTO> updateProduct(@PathVariable Long productId, @Valid @RequestBody UpdateProductRequest request) {

        productService.updateProduct(productId, request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Product updated successfully")
                        .build()
        );
    }



    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/similar")
    public ResponseEntity<ResponseDTO> viewSimilarProducts(@RequestParam Long productId, @RequestParam Map<String,String> allParams) {

        RequestParams requestParams = extractor.extract(allParams);

        ProductListResponseDTO response = productService.getSimilarProducts(productId, requestParams);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Similar products fetched successfully")
                        .data(response)
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ResponseDTO> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product deactivated successfully")
                        .data(null)
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<ResponseDTO> activateProduct(@PathVariable Long id) {

        productService.activateProduct(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product activated successfully")
                        .data(null)
                        .build()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> getProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        Object response;

        switch (role) {

            case "ROLE_ADMIN":
                response = productService.viewProductAsAdmin(id);
                break;

            case "ROLE_SELLER":
                response = productService.getProduct(id);
                break;

            case "ROLE_CUSTOMER":
                response = productService.viewProductAsCustomer(id);
                break;

            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseDTO.builder()
                                .status("FAIL")
                                .message("Unauthorized role")
                                .build());
        }

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }


    @GetMapping
    public ResponseEntity<ResponseDTO> getProducts(@RequestParam(required = false) Map<String, String> allParams, @RequestParam(required = false) Long categoryId, @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseDTO.builder()
                            .status("FAIL")
                            .message("Authentication required")
                            .build());
        }

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        Object response;

        switch (role) {

            case "ROLE_ADMIN":
                RequestParams adminParams = extractor.extract(allParams != null ? allParams : Map.of());
                response = productService.viewAllProductsAsAdmin(adminParams);
                break;

            case "ROLE_SELLER":
                RequestParams sellerParams = extractor.extract(allParams != null ? allParams : Map.of());
                response = productService.getAllProducts(sellerParams);
                break;

            case "ROLE_CUSTOMER":
                RequestParams customerParams = extractor.extract(allParams != null ? allParams : Map.of());
                response = productService.getAllProductsAsCustomer(categoryId, customerParams);
                break;

            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseDTO.builder()
                                .status("FAIL")
                                .message("Unauthorized role")
                                .build());
        }

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .message("Products fetched successfully")
                        .data(response)
                        .build()
        );
    }

}
