package com.bootcamp.ecommerce.DTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDTO {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Size(min = 10, max = 10, message = "Phone number must be valid")
    private String phoneNumber;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm password is mandatory")
    private String confirmPassword;

    @NotBlank(message = "First name is mandatory")
    @Pattern(regexp = "^[A-Za-z\\p{L} ]+$", message = "First name must contain only letters")
    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z\\p{L} ]+$", message = "Middle name must contain only letters")
    private String middleName;


    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    @Pattern(regexp = "^[A-Za-z\\p{L} ]+$", message = "Last name must contain only letters")
    private String lastName;
}
