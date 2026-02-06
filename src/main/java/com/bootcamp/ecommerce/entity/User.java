package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String middleName;
    private String lastName;

    private Boolean isActive = true;
    private Boolean isDeleted = false;
    private Boolean isLocked = false;
    private Boolean isExpired = false;

    private Integer invalidAttemptCount;

    @Column(name = "password_update_date")
    private LocalDateTime passwordUpdateDate;


    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;

}
