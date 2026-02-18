package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.AdminUserSearchRequestDTO;
import com.bootcamp.ecommerce.DTO.CustomerListResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerListResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/customers")
    public ResponseDTO getAllCustomers(AdminUserSearchRequestDTO request) {

        CustomerListResponseDTO data =
                adminUserService.getAllCustomers(request);

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Customers fetched successfully")
                .data(data)
                .build();
    }

    @GetMapping("/sellers")
    public ResponseDTO getAllSellers(
            @ModelAttribute AdminUserSearchRequestDTO request) {

        SellerListResponseDTO data =
                adminUserService.getAllSellers(request);

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Sellers fetched successfully")
                .data(data)
                .build();
    }

    @PatchMapping("/{customerId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO activateCustomer(@PathVariable Long customerId) {

        return adminUserService.activateCustomer(customerId);

    }


    @PatchMapping("/{customerId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO deactivateCustomer(@PathVariable Long customerId) {

       return adminUserService.deactivateCustomer(customerId);

    }

}
