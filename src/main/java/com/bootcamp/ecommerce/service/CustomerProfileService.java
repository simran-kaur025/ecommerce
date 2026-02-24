package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.CustomerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.UpdateProfileRequestDTO;

public interface CustomerProfileService {
    CustomerProfileResponseDTO getMyProfile();
    ResponseDTO getMyAddress();
    void updateProfile(UpdateProfileRequestDTO request);
    void addAddress(AddressDTO request);
    void deleteAddress(Long addressId);
}
