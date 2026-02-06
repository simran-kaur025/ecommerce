package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_metadata_fields")
public class CategoryMetadataField extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}


