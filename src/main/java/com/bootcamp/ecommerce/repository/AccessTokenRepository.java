package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.AccessToken;
import com.bootcamp.ecommerce.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByToken(String token);
    @Modifying
    @Query(
            value = """
        UPDATE access_tokens
        SET status = 2
        WHERE user_id = :userId
        AND status = 1
    """,
            nativeQuery = true
    )
    void revokeAllByUser(Long userId);
}
