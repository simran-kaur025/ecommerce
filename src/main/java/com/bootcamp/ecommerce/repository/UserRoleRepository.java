package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>  {
    boolean existsByUserAndRoleAuthority(User user, String authority);
}
