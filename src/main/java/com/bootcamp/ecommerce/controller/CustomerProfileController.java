package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.CustomerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.UpdateProfileRequestDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/profile")
    public ResponseEntity<ResponseDTO> viewMyProfile() {

        CustomerProfileResponseDTO profile = customerProfileService.getMyProfile();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(profile)
                        .build()
        );
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/addresses")
    public ResponseEntity<ResponseDTO> viewMyAddresses() {

        ResponseDTO addressDTO = customerProfileService.getMyAddress();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(addressDTO)
                        .build()
        );

    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/update/profile")
    public ResponseEntity<ResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileRequestDTO request) {

        customerProfileService.updateProfile(request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Profile updated successfully")
                        .build()
        );
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add/address")
    public ResponseEntity<ResponseDTO> addAddress(@Valid @RequestBody AddressDTO request) {

        customerProfileService.addAddress(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ResponseDTO.builder()
                                .status("CREATED")
                                .data("Address added successfully")
                                .build()
                );
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/delete/address/{addressId}")
    public ResponseEntity<ResponseDTO> deleteAddress(
            @PathVariable Long addressId) {

        customerProfileService.deleteAddress(addressId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data("Address deleted successfully")
                        .build()
        );
    }



}

