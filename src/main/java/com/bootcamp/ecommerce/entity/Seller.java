package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sellers")
public class Seller extends Auditable {

    @Id
    private Long userId;

    @Column(unique = true, nullable = false)
    private String gst;

    @Column(nullable = false)
    private String companyName;

    @Column(length = 10)
    private String companyContact;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}

