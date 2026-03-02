package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Category;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.entity.CategoryMetadataFieldValue;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryMetadataFieldValueRepository extends JpaRepository<CategoryMetadataFieldValue, Long> {

    Optional<CategoryMetadataFieldValue> findByCategoryIdAndCategoryMetadataFieldId(Long categoryId, Long fieldId);

    List<CategoryMetadataFieldValue> findByCategory(Category category);

    Optional<CategoryMetadataFieldValue> findById(Long id);

    @Query(value = """
        SELECT *
        FROM category_meta_field_values v
        WHERE v.category_id IN (:categoryIds)
        """,
            nativeQuery = true)
    List<CategoryMetadataFieldValue> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);





}
