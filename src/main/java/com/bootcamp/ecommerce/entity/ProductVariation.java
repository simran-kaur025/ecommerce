package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_variations")
public class ProductVariation extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity_available;

    private Double price;

    private String primaryImageName;

    private Boolean isActive = true;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

}
