package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long>{
    boolean existsByGst(String Gst);
    Page<Seller> findByUserEmailContainingIgnoreCase(String email, Pageable pageable);
}

