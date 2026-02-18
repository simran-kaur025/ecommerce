package com.bootcamp.ecommerce.DTO;

import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private boolean active;
}
