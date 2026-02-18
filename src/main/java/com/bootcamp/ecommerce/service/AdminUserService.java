package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.AdminUserSearchRequestDTO;
import com.bootcamp.ecommerce.DTO.CustomerListResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerListResponseDTO;


public interface AdminUserService {
    CustomerListResponseDTO getAllCustomers(AdminUserSearchRequestDTO request);
    SellerListResponseDTO getAllSellers(AdminUserSearchRequestDTO request);
    ResponseDTO activateCustomer(Long customerId);
    ResponseDTO deactivateCustomer(Long customerId);
}
