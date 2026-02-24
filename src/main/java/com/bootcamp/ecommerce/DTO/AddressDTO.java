package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class    AddressDTO {

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @NotBlank(message = "Address line is required")
    @Size(min = 5, max = 255, message = "Address line must be between 5 and 255 characters")
    private String addressLine;

    @NotBlank(message = "Zip code is required")
    @Pattern(
            regexp = "^\\d{5,6}$",
            message = "Zip code must be 5 or 6 digits"
    )
    private String zipCode;

    @Size(min = 2, max = 50, message = "Label must be between 2 and 50 characters")
    private String label;
}

