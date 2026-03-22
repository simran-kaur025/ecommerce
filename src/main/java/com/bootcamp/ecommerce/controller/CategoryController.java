package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.CategoryService;
import com.bootcamp.ecommerce.utils.RequestParamsExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final RequestParamsExtractor extractor;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseDTO> addCategory(@Valid @RequestBody CategoryRequestDTO request, Locale locale) {

        ResponseDTO response = categoryService.addCategory(request,locale);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> viewOneCategory(@PathVariable Long id) {

        CategoryResponse response = categoryService.getOneCategory(id);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }
    @GetMapping("/all/admin/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> viewAllCategories(@RequestParam Map<String, String> allParams) {

        RequestParams requestParams = extractor.extract(allParams);

        Page<CategoryResponse> pageData = categoryService.getAllCategories(requestParams);

        PageResponse<CategoryResponse> response = new PageResponse<>(pageData);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(response)
                        .build()
        );
    }
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN','SELLER','CUSTOMER')")
//    public ResponseEntity<ResponseDTO> getAllCategories(@RequestParam Map<String, String> allParams, @RequestParam(required = false) Long categoryId , @AuthenticationPrincipal UserDetails userDetails) {
//
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ResponseDTO.builder()
//                            .status("FAIL")
//                            .message("Authentication required")
//                            .build());
//        }
//
//        String role = userDetails.getAuthorities().stream()
//                .findFirst()
//                .map(Object::toString)
//                .orElse("");
//
//        Object response;
//
//        switch (role) {
//            case "ROLE_ADMIN":
//                RequestParams requestParams = extractor.extract(allParams != null ? allParams : Map.of());
//                Page<CategoryResponse> pageData = categoryService.getAllCategories(requestParams);
//                 response = new PageResponse<>(pageData);
//                break;
//
//            case "ROLE_SELLER":
//                response = categoryService.getAllCategoriesAsSeller();
//                break;
//
//            case "ROLE_CUSTOMER":
//                response = categoryService.getCategoriesAsCustomer(categoryId);
//                break;
//
//            default:
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(ResponseDTO.builder()
//                                .status("FAIL")
//                                .message("Unauthorized role")
//                                .build());
//        }
//
//        return ResponseEntity.ok(
//                ResponseDTO.builder()
//                        .status(Constant.SUCCESS)
//                        .data(response)
//                        .build()
//        );
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/metadata-values")
    public ResponseEntity<ResponseDTO> addMetadataToCategory( @Valid @RequestBody AddCategoryMetadataRequest request) {

        categoryService.addMetadataToCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Metadata field added successfully")
                        .build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/metadata-values")
    public ResponseEntity<ResponseDTO> updateValues(@RequestBody AddCategoryMetadataRequest request) {

        categoryService.updateMetadataValues(request);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("Metadata values updated successfully")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ResponseDTO> updateCategory( @Valid @RequestBody BasicCategoryDTO request) {

        categoryService.updateCategory(request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Category updated successfully")
                        .build()
        );
    }

    @GetMapping("/all/seller")
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


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/all/customer")
    public ResponseEntity<ResponseDTO> getCategoriesAsCustomer(@RequestParam(required = false) Long categoryId) {

        List<CategoryResponse> data = categoryService.getCategoriesAsCustomer(categoryId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(data)
                        .build()
        );
    }


    @GetMapping("/{categoryId}/filters")
    public ResponseEntity<FilterResponse> getFilterData(@PathVariable Long categoryId) {

        FilterResponse response = categoryService.getFilterData(categoryId);
        return ResponseEntity.ok(response);
    }



}
