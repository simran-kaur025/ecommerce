package com.bootcamp.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class User extends Auditable {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id")
    private UUID id;

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
