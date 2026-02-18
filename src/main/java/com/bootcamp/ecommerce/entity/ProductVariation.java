package com.bootcamp.ecommerce.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;

import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product_variations")
@Getter
@Setter
public class ProductVariation extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity_available;

    private Double price;

    private String primaryImageName;

    private Boolean isActive = true;

    private List<String> secondaryImages;



    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, List<String>> metadata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

}
