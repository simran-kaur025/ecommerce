package com.bootcamp.ecommerce.entity;

import com.bootcamp.ecommerce.enums.OrderState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="order_status")
@Getter
@Setter
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;

    @Enumerated(EnumType.STRING)
    private OrderState fromStatus;

    @Enumerated(EnumType.STRING)
    private OrderState toStatus;

    private LocalDateTime updatedAt;
}
