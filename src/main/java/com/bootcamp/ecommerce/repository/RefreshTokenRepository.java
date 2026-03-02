package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.RefreshToken;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
    Optional<RefreshToken> findByUserAndStatus(User user, Integer status);

    @Modifying
    @Query(
            value = """
        UPDATE refresh_tokens
        SET status = 2
        WHERE user_id = :userId
        AND status = 1
    """,
            nativeQuery = true
    )
    void revokeAllByUser(Long userId);
}

