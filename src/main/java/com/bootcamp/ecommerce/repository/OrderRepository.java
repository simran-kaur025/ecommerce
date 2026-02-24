package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
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
    @Query("""
       SELECT o FROM Order o
       WHERE o.customer.id = :customerId
       AND CAST(o.id AS string) LIKE CONCAT('%', :query, '%')
       """)
    Page<Order> searchMyOrders(Long customerId, String query, Pageable pageable);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

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
