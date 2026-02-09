package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private boolean active;
}
