package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "category_metadata_field_values")
public class CategoryMetadataFieldValue extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField metadataField;

    @Column(nullable = false)
    private String values; // comma separated values
}
