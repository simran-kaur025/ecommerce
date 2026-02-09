package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.entity.ActivationToken;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.repository.ActivationTokenRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.ActivationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationTokenServiceImpl implements ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;

    private static final int TOKEN_VALID_HOURS = 3;

    @Override
    public ActivationToken createToken(User user) {


        if (user == null) {
            throw new IllegalStateException("User cannot be null for activation token");
        }


        activationTokenRepository.deleteByUser(user);
        activationTokenRepository.flush();

        ActivationToken token = new ActivationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryTime(LocalDateTime.now().plusHours(TOKEN_VALID_HOURS));

        return activationTokenRepository.save(token);
    }

    @Override
    public boolean validateToken(String token) {

        Optional<ActivationToken> optionalToken =
                activationTokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return false;
        }

        ActivationToken activationToken = optionalToken.get();
        return activationToken.getExpiryTime().isAfter(LocalDateTime.now());
    }

    @Override
    public void deleteOldToken(String token) {
        if (token != null && activationTokenRepository.existsByToken(token)) {
            activationTokenRepository.deleteByToken(token);
        }
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void deleteExpiredTokens() {
        activationTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }

}

