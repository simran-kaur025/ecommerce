package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.CustomerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.UpdateProfileRequestDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

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
    @GetMapping("/address")
    public ResponseEntity<ResponseDTO> viewMyAddresses() {

        ResponseDTO addressDTO = customerProfileService.getMyAddress();

        return ResponseEntity.ok(addressDTO);

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
    @PostMapping("/address")
    public ResponseEntity<ResponseDTO> addAddress(@Valid @RequestBody AddressDTO request) {

        ResponseDTO responseDTO = customerProfileService.addAddress(request);

        HttpStatus status = "CREATED".equals(responseDTO.getStatus())
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        return ResponseEntity
                .status(status)
                .body(responseDTO);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/address/{addressId}")
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

