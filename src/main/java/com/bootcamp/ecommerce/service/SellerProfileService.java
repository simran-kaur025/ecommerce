package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.SellerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileUpdateRequestDTO;

public interface SellerProfileService {
    SellerProfileResponseDTO getMyProfile();
    void updateSellerProfile(SellerProfileUpdateRequestDTO request);
}

