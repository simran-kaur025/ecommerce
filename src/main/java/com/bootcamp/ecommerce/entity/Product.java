package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="products")
@Getter
@Setter
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
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

}
