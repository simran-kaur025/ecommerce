package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CustomerListResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerListResponseDTO;


public interface AdminUserService {
    ResponseDTO getAllCustomers(int pageSize,int offSet, String customSort,String email);
    ResponseDTO getAllSellers(int pageSize,int offSet, String customSort,String email);
    ResponseDTO activateUser(Long userId);
    ResponseDTO deactivateUser(Long userId);
}
