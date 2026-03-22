package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.ForgotPasswordToken;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, Long> {

    Optional<ForgotPasswordToken> findByToken(String token);

    void deleteByUser(User user);
}

