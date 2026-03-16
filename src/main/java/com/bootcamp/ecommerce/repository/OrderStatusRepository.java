package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.OrderStatus;
import com.bootcamp.ecommerce.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    OrderStatus findTopByOrderProductOrderByTransitionDateDesc(OrderProduct orderProduct);
    OrderStatus findTopByOrderProductIdOrderByTransitionDateDesc(Long orderProductId);
    Optional<OrderState> findByFromStatusAndToStatus(OrderState fromStatus, OrderState toStatus);

}