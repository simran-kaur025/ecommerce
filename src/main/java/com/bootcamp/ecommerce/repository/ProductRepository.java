package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {
    boolean existsByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategoryAndSeller(String name, String brand, Category category, Seller seller);

    Page<Product> findBySeller_User_EmailAndIsDeletedFalse(String email,Pageable pageable);

    Page<Product> findBySeller_User_EmailAndIsDeletedFalseAndNameContainingIgnoreCase(String email, String name, Pageable pageable);

    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndCategory_IdAndSeller_Id(String name, String brandName, Long categoryId,Long sellerId);

    Page<Product> findAll(Specification specification, Pageable pageable);


     boolean existsByCategoryIdAndIsDeletedFalse(Long id);

    @Query(value = """
        SELECT DISTINCT p.brand
        FROM products p
        WHERE p.category_id IN (:categoryIds)
        """,
            nativeQuery = true)
    List<String> findDistinctBrandByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

}
