package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class SellerProfileResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;

    private String companyName;
    private String companyContact;
    private String gst;

    private AddressDTO address;
}

