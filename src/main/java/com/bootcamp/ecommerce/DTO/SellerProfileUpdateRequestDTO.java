package com.bootcamp.ecommerce.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerProfileUpdateRequestDTO {

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "firstName must contain only letters"
    )
    private String firstName;

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "lastName must contain only letters"
    )
    private String lastName;

    @Pattern(
            regexp="^[1-9][0-9]{9}$",
            message = "Phone number must be 10 digit and must not start with 0"
    )
    private String companyContact;

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "Middle name must contain only letters"
    )
    private String middleName;

    @Valid
    private UpdateAddressRequestDTO address;
}
