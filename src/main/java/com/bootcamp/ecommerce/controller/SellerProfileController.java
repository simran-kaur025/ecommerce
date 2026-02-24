package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileUpdateRequestDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.SellerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;
    @GetMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO> viewMyProfile() {

        SellerProfileResponseDTO profile = sellerProfileService.getMyProfile();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .data(profile)
                        .build()
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/update/profile")
    public ResponseDTO updateProfile(@Valid @RequestBody SellerProfileUpdateRequestDTO request) {

        sellerProfileService.updateSellerProfile(request);

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Profile updated successfully")
                .build();
    }

}
