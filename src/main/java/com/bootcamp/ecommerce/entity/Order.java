package com.bootcamp.ecommerce.entity;

import com.bootcamp.ecommerce.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private Double amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;


    @Column(name = "customer_address_address_line", nullable = false)
    private String customerAddressAddressLine;

    @Column(name = "customer_address_city", nullable = false)
    private String customerAddressCity;

    @Column(name = "customer_address_state", nullable = false)
    private String customerAddressState;

    @Column(name = "customer_address_country", nullable = false)
    private String customerAddressCountry;

    @Column(name = "customer_address_zip_code", nullable = false)
    private String customerAddressZipCode;

    @Column(name = "customer_address_label")
    private String customerAddressLabel;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
}

