package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_role")
public class UserRole extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;


}
