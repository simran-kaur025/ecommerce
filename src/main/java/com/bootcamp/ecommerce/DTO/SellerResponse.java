package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerResponse {
    private UUID id;
    private String name;
    private String email;
    private boolean active;
    private String companyName;
    private String companyContact;
}
