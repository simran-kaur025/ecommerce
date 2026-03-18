package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.ChangePasswordRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;

import java.util.Locale;

public interface AuthService {
    ResponseDTO logout(String refreshToken);
    ResponseDTO refreshAccessToken(String accessTokenValue);
    ResponseDTO forgotPassword(String email);
    ResponseDTO resetPassword(String token, String password, String confirmPassword);
    void changePassword(ChangePasswordRequestDTO request, Locale locale);
}

