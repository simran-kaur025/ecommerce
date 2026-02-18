package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, Long> {
    boolean existsByName(String name);
}