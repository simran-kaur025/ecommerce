package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.entity.ActivationToken;
import com.bootcamp.ecommerce.entity.User;

public interface ActivationTokenService{
    ActivationToken createToken(User user);
    boolean validateToken(String token);
    void deleteOldToken(String token);
}
