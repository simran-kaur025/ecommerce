package com.bootcamp.ecommerce.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer extends Auditable{

    @Id
    private Long userId;

    @Column(length = 10)
    private String contact;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
