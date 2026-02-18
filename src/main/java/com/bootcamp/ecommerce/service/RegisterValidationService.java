package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
import com.bootcamp.ecommerce.DTO.SellerRequestDTO;
import com.bootcamp.ecommerce.DTO.UserValidationDTO;

import java.util.List;

public interface RegisterValidationService {
    void validateCustomer(CustomerRequestDTO request, List<UserValidationDTO> validationErrors);
    void validateSeller(SellerRequestDTO request, List<UserValidationDTO> errors);
}

