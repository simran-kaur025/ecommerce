package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.DTO.PageResponse;
import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long>, JpaSpecificationExecutor<ProductVariation> {

    List<ProductVariation> findByProduct(Product product);
    Page<ProductVariation>  findAll(Specification specification, Pageable pageable);
   List<ProductVariation>findByProductAndIsActiveTrue(Product product);
    @Query(value = """
        SELECT MIN(v.price), MAX(v.price)
        FROM product_variation v
        JOIN product p ON v.product_id = p.id
        WHERE p.category_id IN (:categoryIds)
        """, nativeQuery = true)
    Object[] findMinMaxPriceByCategories(@Param("categoryIds") List<Long> categoryIds);

}

