package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query(value = """
    SELECT DISTINCT o.*
    FROM orders o
    JOIN users c ON o.customer_id = c.id
    LEFT JOIN order_products op ON op.order_id = o.id
    LEFT JOIN order_status os ON os.order_product_id = op.id
    WHERE CAST(o.id AS TEXT) ILIKE CONCAT('%', :query, '%')
       OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(CAST(os.to_status AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))
""",
            nativeQuery = true)
    Page<Order> searchOrders(@Param("query") String query, Pageable pageable);

    Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);

    Page<Order> findAll(Specification specification, Pageable pageable);

    @Query(value = """
    SELECT DISTINCT o.*
    FROM orders o
    JOIN order_products op ON op.order_id = o.id
    JOIN products p ON p.id = op.product_id
    WHERE p.seller_user_id = :sellerId
      AND (:query IS NULL OR CAST(o.id AS CHAR) LIKE CONCAT('%', :query, '%'))
""",
            nativeQuery = true)
    Page<Order> findOrdersBySellerProducts(Long sellerId, String query, Pageable pageable);



}
