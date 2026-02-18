package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
