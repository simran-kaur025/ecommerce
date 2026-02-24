package com.bootcamp.ecommerce.service.cli;

import com.bootcamp.ecommerce.entity.Role;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.entity.UserRole;
import com.bootcamp.ecommerce.repository.RoleRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.repository.UserRoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2)
@RequiredArgsConstructor
public class CreateAdmin {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    @PostConstruct
    public void initAdmin() {

        String email = "simrankaur252004@gmail.com";

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            System.out.println("Admin already exists.");
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User admin = new User();
        admin.setEmail(email);
        admin.setFirstName("Simran");
        admin.setLastName("kaur");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setIsActive(true);
        admin.setIsDeleted(false);
        admin.setIsLocked(false);
        admin.setIsExpired(false);

        userRepository.save(admin);
        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        System.out.println("Admin user created.");
    }
}