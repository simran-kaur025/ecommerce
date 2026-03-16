package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerRequestDTO;

import java.util.Locale;

public interface RegisterService {

     ResponseDTO registerCustomer(CustomerRequestDTO requestDTO, Locale locale);

     ResponseDTO registerSeller(SellerRequestDTO requestDTO, Locale locale);

     ResponseDTO activateAccount(String token);

     ResponseDTO resendActivationToken(String email);

}
