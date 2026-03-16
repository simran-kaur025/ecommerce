package com.bootcamp.ecommerce.service.cli;

import com.bootcamp.ecommerce.entity.Role;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.entity.UserRole;
import com.bootcamp.ecommerce.repository.RoleRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2)
@RequiredArgsConstructor
public class CreateAdmin implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isPresent()) {
            System.out.println("Admin already exists.");
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFirstName("Simran");
        admin.setLastName("Kaur");
        admin.setPassword(passwordEncoder.encode(adminPassword));
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