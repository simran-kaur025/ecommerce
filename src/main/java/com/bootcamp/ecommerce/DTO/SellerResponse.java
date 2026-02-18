package com.bootcamp.ecommerce.DTO;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerResponse {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private String companyName;
    private String companyContact;
}
