package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAddressRequestDTO {

    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    @Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
    private String state;

    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @Size(min = 2, max = 100, message = "Address Line must be between 2 and 100 characters")
    private String addressLine;

    @Pattern(regexp = "^\\d{5,6}$", message = "Zip code must be 5 or 6 digits")
    private String zipCode;

    @Size(min = 2, max = 100, message = "Label must be between 2 and 100 characters")
    private String label;
}
