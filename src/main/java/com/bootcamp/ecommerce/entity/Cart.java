package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "cart")
@Getter
@Setter
public class Cart extends Auditable {

    @EmbeddedId
    private CartId id;

    private Integer quantity;

    private Boolean isWishlistItem = false;

    @MapsId("customerId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @MapsId("productVariationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;
}
