package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query(value = """
    SELECT *
    FROM order_products
    WHERE current_status = :status
""", nativeQuery = true)
    List<OrderProduct> findPendingSellerActionOrderProducts(@Param("status") String status);
}
