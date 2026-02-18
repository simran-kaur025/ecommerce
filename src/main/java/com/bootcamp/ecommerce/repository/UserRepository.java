package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role
    WHERE u.email = :email
""")
Optional<User> findByEmailWithRoles(@Param("email") String email);



}

