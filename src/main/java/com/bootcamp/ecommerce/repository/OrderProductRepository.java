package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

}
