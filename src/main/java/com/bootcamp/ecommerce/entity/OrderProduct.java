package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name="order_products")
public class OrderProduct extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;

    private Integer quantity;

    private Double price;

}
