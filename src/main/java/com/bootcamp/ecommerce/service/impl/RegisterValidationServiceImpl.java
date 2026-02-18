package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
import com.bootcamp.ecommerce.DTO.SellerRequestDTO;
import com.bootcamp.ecommerce.DTO.UserValidationDTO;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.repository.CustomerRepository;
import com.bootcamp.ecommerce.repository.SellerRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.repository.UserRoleRepository;
import com.bootcamp.ecommerce.service.RegisterValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class RegisterValidationServiceImpl implements RegisterValidationService {

    private  final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final UserRoleRepository userRoleRepository;

    @Override

    public void validateCustomer(CustomerRequestDTO request, List<UserValidationDTO> errors) {

        // EMAIL
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Email")
                    .errors(List.of("Email is required"))
                    .build());
        } else if (!request.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add(UserValidationDTO.builder()
                    .key("Email")
                    .errors(List.of("Invalid email format"))
                    .build());
        } else {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(user -> {
                        if (userRoleRepository.existsByUserAndRoleAuthority(user, "ROLE_CUSTOMER")) {
                            errors.add(UserValidationDTO.builder()
                                    .key("Email")
                                    .errors(List.of("User is already registered as customer"))
                                    .build());
                        }
                    });
        }

        // PHONE
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Phone Number")
                    .errors(List.of("Phone number is required"))
                    .build());
        } else if (!request.getPhoneNumber().matches("^[6-9]\\d{9}$")) {
            errors.add(UserValidationDTO.builder()
                    .key("Phone Number")
                    .errors(List.of(
                            "Phone number must be 10 digits and should be valid"
                    ))
                    .build());
        } else if (customerRepository.existsByContact(
                request.getPhoneNumber())) {
            errors.add(UserValidationDTO.builder()
                    .key("Phone Number")
                    .errors(List.of("Phone number already exists"))
                    .build());
        }

        // PASSWORD
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Password")
                    .errors(List.of("Password is required"))
                    .build());
        } else if (!isStrongPassword(request.getPassword())) {
            errors.add(UserValidationDTO.builder()
                    .key("Password")
                    .errors(List.of(
                            "Password must contain upper, lower, number and special character"
                    ))
                    .build());
        }

        // CONFIRM PASSWORD
        if (request.getConfirmPassword() == null ||
                !request.getPassword().equals(request.getConfirmPassword())) {

            errors.add(UserValidationDTO.builder()
                    .key("Confirm Password")
                    .errors(List.of("Password and confirm password do not match"))
                    .build());
        }
    }


    @Override
    public void validateSeller(SellerRequestDTO request,
                               List<UserValidationDTO> errors) {

        // EMAIL
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Email")
                    .errors(List.of("Email is required"))
                    .build());
        } else if (!request.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add(UserValidationDTO.builder()
                    .key("Email")
                    .errors(List.of("Invalid email format"))
                    .build());
        }
        else {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(user -> {
                        if (userRoleRepository
                                .existsByUserAndRoleAuthority(user, "ROLE_SELLER")) {
                            errors.add(UserValidationDTO.builder()
                                    .key("Email")
                                    .errors(List.of("User is already registered as Seller"))
                                    .build());
                        }
                    });
        }



        // PASSWORD
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Password")
                    .errors(List.of("Password is required"))
                    .build());
        } else if (!isStrongPassword(request.getPassword())) {
            errors.add(UserValidationDTO.builder()
                    .key("Password")
                    .errors(List.of(
                            "Password must contain upper, lower, number and special character"
                    ))
                    .build());
        }

        // CONFIRM PASSWORD
        if (request.getConfirmPassword() == null ||
                !request.getPassword().equals(request.getConfirmPassword())) {

            errors.add(UserValidationDTO.builder()
                    .key("Confirm Password")
                    .errors(List.of("Password and confirm password do not match"))
                    .build());
        }

        //GST
        if (sellerRepository.existsByGst(request.getGst())) {
            errors.add(UserValidationDTO.builder()
                    .key("gst")
                    .errors(List.of("GST already registered"))
                    .build());
        }

        //Company
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Company Name")
                    .errors(List.of("Company name is required"))
                    .build());
        } else if (sellerRepository.existsByCompanyName(request.getCompanyName())) {
            errors.add(UserValidationDTO.builder()
                    .key("Company Name")
                    .errors(List.of("Company name is already registered"))
                    .build());
        }

        // company contact

        //Company
        if (request.getCompanyContact() == null || request.getCompanyContact().isBlank()) {
            errors.add(UserValidationDTO.builder()
                    .key("Company contact")
                    .errors(List.of("Company contact is required"))
                    .build());
        } else if (sellerRepository.existsByCompanyName(request.getCompanyName())) {
            errors.add(UserValidationDTO.builder()
                    .key("Company contact")
                    .errors(List.of("Company contact is already registered"))
                    .build());
        }
    }


    private boolean isStrongPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$"
        );
    }


}

