package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtTokenService {
    public String generateAccessToken(User user);
    public String generateRefreshToken(User user);
    public Date getRefreshTokenExpiryDate();

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
