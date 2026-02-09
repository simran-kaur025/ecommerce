package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.ActivationToken;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);

   void deleteByUser(User user);
   boolean existsByToken(String token);
   void deleteByToken(String token);
   void deleteByExpiryTimeBefore(LocalDateTime time);
}

