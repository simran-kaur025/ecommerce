package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCaseAndParentCategoryIsNull(String name);

    boolean existsByNameIgnoreCaseAndParentCategoryId(String name, Long parentId);

    Optional<Category> findById(Long id);
    List<Category> findByParentCategory(Category category);
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Category> findAll(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndParentCategoryIsNullAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCaseAndParentCategoryIdAndIdNot(
            String name,
            Long parentId,
            Long id
    );

    List<Category> findByParentCategoryId(Long categoryId);
    boolean existsByNameIgnoreCaseAndParentCategory(String name, Category parent);


    @Query("""
       SELECT c FROM Category c
       WHERE NOT EXISTS (
           SELECT 1 FROM Category child
           WHERE child.parentCategory = c
       )
       """)
    List<Category> findLeafCategories();

    List<Category> findByParentCategoryIsNull();
    boolean existsByParentCategoryId(Long parentId);

}

