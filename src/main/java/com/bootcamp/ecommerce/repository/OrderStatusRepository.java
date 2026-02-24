package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    OrderStatus findTopByOrderProductOrderByTransitionDateDesc(OrderProduct orderProduct);
    OrderStatus findTopByOrderProductIdOrderByTransitionDateDesc(Long orderProductId);

}