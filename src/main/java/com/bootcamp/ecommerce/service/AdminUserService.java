package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CustomerListResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerListResponseDTO;


public interface AdminUserService {
    CustomerListResponseDTO getAllCustomers(int pageSize,int offSet, String customSort,String email);
    SellerListResponseDTO getAllSellers(int pageSize,int offSet, String customSort,String email);
    ResponseDTO activateCustomer(Long customerId);
    ResponseDTO deactivateCustomer(Long customerId);
}
