package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name="products")
public class Product extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String brand;

    private Boolean isCancellable;
    private Boolean isReturnable;

    private Boolean isActive = true;
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_user_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

}
