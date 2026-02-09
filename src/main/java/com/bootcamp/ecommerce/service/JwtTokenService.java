package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.entity.User;

import java.util.Date;

public interface JwtTokenService {
    public String generateAccessToken(User user);
    public String generateRefreshToken(User user);
    public Date getRefreshTokenExpiryDate();
}
