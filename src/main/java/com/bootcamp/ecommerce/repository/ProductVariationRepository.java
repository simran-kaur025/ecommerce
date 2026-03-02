package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    Page<ProductVariation> findByProduct_Id(Long productId, Pageable pageable);

    Page<ProductVariation> findByProduct_IdAndMetadataContainingIgnoreCase(Long productId, String metadata, Pageable pageable);
    List<ProductVariation> findByProduct(Product product);
  List<ProductVariation>findByProductAndIsActiveTrue(Product product);
    @Query(value = """
        SELECT MIN(v.price), MAX(v.price)
        FROM product_variations v
        JOIN products p ON v.product_id = p.id
        WHERE p.category_id IN (:categoryIds)
        """, nativeQuery = true)
    Object[] findMinMaxPriceByCategories(@Param("categoryIds") List<Long> categoryIds);

}

