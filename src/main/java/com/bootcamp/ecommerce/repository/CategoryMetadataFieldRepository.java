package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.DTO.MetadataFieldDTO;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, Long>, JpaSpecificationExecutor<CategoryMetadataField> {
    boolean existsByNameIgnoreCase(String name);
    Page<CategoryMetadataField> findByNameContainingIgnoreCase(String name, Pageable pageable);
}