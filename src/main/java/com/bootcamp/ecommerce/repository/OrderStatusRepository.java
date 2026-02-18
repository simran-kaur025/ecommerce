package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
}
