package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.UpdateAddressRequestDTO;

public interface AddressService {
    void updateAddress(Long id, UpdateAddressRequestDTO request);
}
