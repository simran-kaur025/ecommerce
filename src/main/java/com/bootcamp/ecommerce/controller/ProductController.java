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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RequestParamsExtractor extractor;


    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/add/products/seller")
    public ResponseEntity<ResponseDTO> addProduct(@Valid @RequestBody ProductRequestDTO request) {

        productService.addProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(Constant.SUCCESS, "Product created successfully",null));
    }


    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/get/product/seller/{productId}")
    public ResponseEntity<ResponseDTO> getProduct(@PathVariable Long productId) {

        ProductResponse response = productService.getProduct(productId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );

    }

    @GetMapping("/get/all/products/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO> getAllProducts(@RequestParam Map<String,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);

        PageResponse<ProductResponse> response = productService.getAllProducts(requestParams);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }




    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/delete/products/seller/{productId}")
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
    @PutMapping("/update/products/seller/{productId}")
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
    @GetMapping("/get/products/customer/{id}")
    public ResponseEntity<ResponseDTO> viewProduct(@PathVariable Long id) {

        ProductDetailResponseDTO response = productService.viewProductAsCustomer(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }



    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/get/all/products/customer")
    public ResponseEntity<ResponseDTO> getProducts(@RequestParam Long categoryId, @RequestParam Map<String,String> allParams) {
        RequestParams requestParams = extractor.extract(allParams);
        PageResponse<ProductDetailResponseDTO> response =  productService.getAllProductsAsCustomer(categoryId, requestParams);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/get/similar/products/customer")
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
    @GetMapping("/get/products/admin/{id}")
    public ResponseEntity<ResponseDTO> viewProductAdmin(@PathVariable Long id) {

        ProductDetailResponseDTO response = productService.viewProductAsAdmin(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/all/products/admin")
    public ResponseEntity<ResponseDTO> viewAllProducts(@RequestParam Map<String,String> allParams) {

        RequestParams requestParams = extractor.extract(allParams);

        PageResponse<ProductDetailResponseDTO> response= productService.viewAllProductsAsAdmin(requestParams);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/products/deactivate/{id}")
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
    @PutMapping("/products/activate/{id}")
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






}
