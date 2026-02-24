package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.ActivationToken;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByContact(String contact);
    Page<Customer> findByUserEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );
    Optional<Customer> findById(Long customerId);
    Optional<Customer> findByUser(User user);
    boolean existsByContactAndIdNot(String phoneNumber, Long id);


}
