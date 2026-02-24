package com.bootcamp.ecommerce.service.cli;

import com.bootcamp.ecommerce.entity.Role;
import com.bootcamp.ecommerce.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
    public class CreateRoles {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {

        List<String> roles = List.of(
                "ROLE_ADMIN",
                "ROLE_SELLER",
                "ROLE_CUSTOMER"
        );

        roles.forEach(roleName -> {
            if (roleRepository.findByAuthority(roleName).isEmpty()) {

                Role role = new Role();
                role.setAuthority(roleName);

                roleRepository.save(role);

                System.out.println(roleName + " created.");
            }
        });

        System.out.println("All roles initialized.");
    }
}
