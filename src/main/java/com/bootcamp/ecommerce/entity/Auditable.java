package com.bootcamp.ecommerce.entity;


import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass // This class is not a table , but its fields will be copied into other tables
@EntityListeners(AuditingEntityListener.class) // automatically listens to save and update database
@Getter
@Setter
public abstract class Auditable  {

    @CreatedDate
    private LocalDateTime dateCreated;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
