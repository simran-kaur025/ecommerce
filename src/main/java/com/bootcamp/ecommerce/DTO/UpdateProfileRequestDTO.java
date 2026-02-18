package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDTO {
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    private String middleName;

    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;

}
