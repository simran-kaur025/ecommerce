package com.bootcamp.ecommerce.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class CartId implements Serializable {

    private Long customerId;
    private Long productVariationId;
}

