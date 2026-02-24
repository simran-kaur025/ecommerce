package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.UpdateAddressRequestDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
/*

*/
public class AddressController {

    private final AddressService addressService;


    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update-addresses/{addressId}")
    public ResponseEntity<ResponseDTO> patchAddress(@PathVariable Long addressId, @RequestBody UpdateAddressRequestDTO request) {
        addressService.updateAddress(addressId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .status(Constant.SUCCESS)
                        .message("Address updated successfully")
                        .build());
    }

}
