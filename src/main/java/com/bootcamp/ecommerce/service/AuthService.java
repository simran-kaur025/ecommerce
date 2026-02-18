package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;

public interface AuthService {
    ResponseDTO login(LoginRequestDTO requestDTO);
    ResponseDTO logout(String refreshToken);
    ResponseDTO refreshAccessToken(String refreshToken);
    ResponseDTO forgotPassword(String email);
    ResponseDTO resetPassword(String token, String password, String confirmPassword);
}

