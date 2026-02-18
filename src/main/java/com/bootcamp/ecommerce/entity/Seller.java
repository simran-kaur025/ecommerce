package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "sellers")
@Getter
@Setter
public class Seller extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String gst;

    @Column(nullable = false)
    private String companyName;

    @Column(length = 10)
    private String companyContact;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;
}


