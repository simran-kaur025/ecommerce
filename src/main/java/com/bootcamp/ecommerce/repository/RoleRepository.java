package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Role;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByAuthority(String authority);
}
