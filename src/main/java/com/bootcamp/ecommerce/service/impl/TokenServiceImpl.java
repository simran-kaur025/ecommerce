package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.repository.AccessTokenRepository;
import com.bootcamp.ecommerce.repository.RefreshTokenRepository;
import com.bootcamp.ecommerce.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void revokeAllTokens(Long userId) {
        accessTokenRepository.revokeAllByUser(userId);
        refreshTokenRepository.revokeAllByUser(userId);
    }
}
