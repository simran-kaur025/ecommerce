package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategoryAndSeller(String name, String brand, Category category, Seller seller);

    Page<Product> findBySeller_User_EmailAndIsDeletedFalse(String email,Pageable pageable);

    Page<Product> findBySeller_User_EmailAndIsDeletedFalseAndNameContainingIgnoreCase(String email, String name, Pageable pageable);

    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategory_IdAndSeller_Id(String name, String brandName, Long categoryId,Long sellerId);

    @Query(value = "SELECT p.* \n" +
            "FROM products p \n" +
            "WHERE p.is_deleted = false \n" +
            "AND p.is_active = true \n" +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) \n" +
            "AND (:sellerId IS NULL OR p.seller_user_id = :sellerId) \n" +
            "AND ( \n" +
            "    :query IS NULL OR \n" +
            "    LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%') OR \n" +
            "    LOWER(p.brand) LIKE CONCAT('%', LOWER(:query), '%') \n" +
            ")",
            nativeQuery = true)
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("query") String query,
            Pageable pageable
    );


    @Query(value = "SELECT p.* \n" +
            "FROM products p \n" +
            "WHERE p.is_deleted = false \n" +
            "AND p.is_active = true \n" +
            "AND p.category_id IN (:categoryIds) \n" +
            "AND ( \n" +
            "    :query IS NULL OR \n" +
            "    LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%') OR \n" +
            "    LOWER(p.brand) LIKE CONCAT('%', LOWER(:query), '%') \n" +
            ")",
            nativeQuery = true)
    Page<Product> searchProductsWithCategory(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("query") String query,
            Pageable pageable
    );


    @Query(value = """
        SELECT * FROM products p
        WHERE p.category_id = :categoryId
        AND p.id <> :productId
        AND p.is_deleted = false
        AND p.is_active = true
        AND (
            :query IS NULL OR
            LOWER(p.name) LIKE CONCAT('%', LOWER(:query), '%') OR
            LOWER(p.brand) LIKE CONCAT('%', LOWER(:query), '%')
        )
        """,
            nativeQuery = true)
    Page<Product> findSimilarProducts(
            @Param("categoryId") Long categoryId,
            @Param("productId") Long productId,
            @Param("query") String query,
            Pageable pageable
    );




}
