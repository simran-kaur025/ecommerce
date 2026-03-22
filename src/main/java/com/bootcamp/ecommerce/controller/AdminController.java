package com.bootcamp.ecommerce.controller;

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
@RequestMapping("/admin")
public class AdminController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public ResponseDTO getAllCustomers(@RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(defaultValue = "0") int offSet,
                                       @RequestParam(defaultValue = "id") String customSort,
                                       @RequestParam(required = false) String email) {

       return adminUserService.getAllCustomers(pageSize,offSet,customSort,email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers")
    public ResponseDTO getAllSellers(@RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(defaultValue = "0") int offSet,
                                     @RequestParam(defaultValue = "id") String customSort,
                                     @RequestParam(required = false) String email) {

        return adminUserService.getAllSellers(pageSize,offSet,customSort,email);
    }

    @PatchMapping("/activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO activateUser(@PathVariable Long userId) {

        return adminUserService.activateUser(userId);

    }


    @PatchMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO deactivateUser(@PathVariable Long userId) {

       return adminUserService.deactivateUser(userId);

    }

}
