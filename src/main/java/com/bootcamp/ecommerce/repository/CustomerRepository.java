package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.ActivationToken;
import com.bootcamp.ecommerce.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByContact(String contact);
    Page<Customer> findByUserEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );
    Optional<Customer> findById(UUID customerId);


}
