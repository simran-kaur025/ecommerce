package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "roles")
public class Role extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String authority;


    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;
}
