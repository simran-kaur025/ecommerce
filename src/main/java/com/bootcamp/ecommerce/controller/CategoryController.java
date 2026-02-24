package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/category")
    public ResponseEntity<ResponseDTO> addCategory(@Valid @RequestBody CategoryRequestDTO request) {

        ResponseDTO response = categoryService.addCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/view/category/{id}")
    public ResponseEntity<ResponseDTO> viewOneCategory(@PathVariable Long id) {

        CategoryResponse response = categoryService.getOneCategory(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/get/all/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> viewAllCategories(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query) {

        Page<CategoryResponse> response = categoryService.getAllCategories(offset, max, sortBy, order, query);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/metadata-fields/values")
    public ResponseEntity<ResponseDTO> addMetadataToCategory( @Valid @RequestBody AddCategoryMetadataRequest request) {

        categoryService.addMetadataToCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Metadata field added successfully")
                        .build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/metadata-values")
    public ResponseEntity<ResponseDTO> updateValues(@RequestBody AddCategoryMetadataRequest request) {

        categoryService.updateMetadataValues(request);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Metadata values updated successfully")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/categories")
    public ResponseEntity<ResponseDTO> updateCategory( @Valid @RequestBody BasicCategoryDTO request) {

        categoryService.updateCategory(request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Category updated successfully")
                        .build()
        );
    }


    /* Seller Category Api */

    @GetMapping("/get/all/categories/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO> getAllCategories(){

        List<LeafCategoryResponse> response = categoryService.getAllCategoriesAsSeller();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }


    /* Customer Category Api */
    @GetMapping("get/all/categories/customer")
    public ResponseDTO getCategoriesAsCustomer(@RequestParam(required = false) Long categoryId) {

        List<CategoryResponse> data = categoryService.getCategoriesAsCustomer(categoryId);

        return ResponseDTO.builder()
                .status("SUCCESS")
                .message("Categories fetched successfully")
                .data(data)
                .build();
    }



}
