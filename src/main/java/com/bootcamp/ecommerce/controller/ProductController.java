package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/add/products/seller")
    public ResponseEntity<ResponseDTO> addProduct(@Valid @RequestBody ProductRequestDTO request) {

        productService.addProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(Constant.SUCCESS, "Product created successfully",null));
    }




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
    public ResponseEntity<ResponseDTO> getAllProducts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query) {

        Page<ProductResponse> response = productService.getAllProducts(offset, max, sortBy, order, query);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

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




    @GetMapping("/get/all/products/customer")
    public ResponseEntity<ResponseDTO> getProducts(
            @RequestParam Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {
        ProductListResponseDTO response =  productService.getAllProductsAsCustomer(categoryId, offset, max, sort, order,query);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }


    @GetMapping("/get/similar/products/customer")
    public ResponseEntity<ResponseDTO> viewSimilarProducts(
            @RequestParam Long productId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {

        ProductListResponseDTO response = productService.getSimilarProducts(productId, query, offset, max, sort, order);

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

    @GetMapping("/get/all/products/admin")
    public ResponseEntity<ResponseDTO> viewAllProducts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String query
    ) {

        ProductListResponseDTO response = productService.viewAllProductsAsAdmin(offset, max, sort, order, categoryId, sellerId, query);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .message("Product fetched successfully")
                        .data(response)
                        .build()
        );
    }


    //    @PreAuthorize("hasRole('ADMIN')")
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


//    @PreAuthorize("hasRole('ADMIN')")
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
