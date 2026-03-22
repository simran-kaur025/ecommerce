package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.OrderStatus;
import com.bootcamp.ecommerce.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    OrderStatus findTopByOrderProductIdOrderByTransitionDateDesc(Long orderProductId);

}