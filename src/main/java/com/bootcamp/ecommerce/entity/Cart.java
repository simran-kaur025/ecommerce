package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart")
public class Cart extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private Boolean isWishlistItem = false;

    @JoinColumn(name = "customer_user_id")
    private User customer;


    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;
}
