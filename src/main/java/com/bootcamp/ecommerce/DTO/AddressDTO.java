package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddressDTO {

    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "State is required")
    private String state;
    @NotBlank(message = "Country is required")
    private String country;
    @NotBlank(message = "Address is required")
    private String addressLine;
    @NotBlank(message = "Zip Code is required")
    private String zipCode;
    private String label;
}

