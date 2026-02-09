package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerRequestDTO;

public interface RegisterService {
     ResponseDTO registerCustomer(CustomerRequestDTO requestDTO);

     ResponseDTO registerSeller(SellerRequestDTO requestDTO);

     ResponseDTO activateAccount(String token);

     ResponseDTO resendActivationToken(String email);

}
