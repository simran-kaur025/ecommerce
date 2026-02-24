package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDTO {

    @Pattern(regexp = "^[A-Za-z]+$", message = "First name must contain only letters")
    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z]*$", message = "Middle name must contain only letters")
    private String middleName;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must contain only letters")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be 10 digits and start with 6–9")
    private String phoneNumber;
}
