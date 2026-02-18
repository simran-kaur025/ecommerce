package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CustomerProfileResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String contact;
    private String image;

}

