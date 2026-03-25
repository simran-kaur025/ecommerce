package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.AddressRepository;
import com.bootcamp.ecommerce.repository.CustomerRepository;
import com.bootcamp.ecommerce.repository.SellerRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AdminUserService;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    private final EmailService emailService;
    private final TokenService tokenService;

    @Override
    public ResponseDTO getAllCustomers(int pageSize,int offSet, String customSort,String email) {

        Map<String,String> sortMap = Map.of(
                "email", "user.email",
                "name", "user.firstName",
                "company", "companyName"
        );

        String sortField = sortMap.getOrDefault(customSort, "id");
        Pageable pageable = PageRequest.of(offSet, pageSize, Sort.by(sortField).ascending());

        Page<Customer> userPage;

        if (email != null && !email.isBlank()) {
            userPage = customerRepository.findByUserEmailContainingIgnoreCase(email, pageable);
        } else {
            userPage = customerRepository.findAll(pageable);
        }

        List<CustomerResponse> customers =
                userPage.getContent().stream()
                        .map(customer -> new CustomerResponse(
                                customer.getUser().getId(),
                                customer.getUser().getFirstName() + " " + customer.getUser().getMiddleName() + " " + customer.getUser().getLastName(),
                                customer.getUser().getEmail(),
                                customer.getUser().getIsActive()
                        ))
                        .toList();

        CustomerListResponseDTO customerListResponse = new CustomerListResponseDTO(
                customers,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber()
        );

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message(customers.isEmpty() ? "No customers found" : "Customers retrieved successfully")
                .data(customerListResponse)
                .build();
    }

    @Override
    public ResponseDTO getAllSellers(int pageSize,int offSet, String customSort,String email) {

        Map<String,String> sortMap = Map.of(
                "email", "user.email",
                "name", "user.firstName",
                "company", "companyName"
        );

        String sortField = sortMap.getOrDefault(customSort, "id");
        Pageable pageable = PageRequest.of(offSet, pageSize, Sort.by(sortField).ascending());

        Page<Seller> sellerPage;

        if (email != null && !email.isBlank()) {
            sellerPage = sellerRepository
                    .findByUserEmailContainingIgnoreCase(email, pageable);
        } else {
            sellerPage = sellerRepository.findAll(pageable);
        }


        List<SellerResponse> sellers = sellerPage.getContent().stream()
                .map(seller -> {
                    AddressDTO addressDTO = addressRepository.findByUser(seller.getUser())
                            .stream()
                            .findFirst()
                            .map(addr -> new AddressDTO(
                                    addr.getAddressLine(),
                                    addr.getCity(),
                                    addr.getState(),
                                    addr.getCountry(),
                                    addr.getZipCode(),
                                    addr.getLabel()
                            ))
                            .orElse(null);

                    return new SellerResponse(
                            seller.getUser().getId(),
                            seller.getUser().getFirstName() + " " + seller.getUser().getLastName(),
                            seller.getUser().getEmail(),
                            seller.getUser().getIsActive(),
                            seller.getCompanyName(),
                            seller.getCompanyContact(),
                            addressDTO
                    );
                })
                .toList();

        SellerListResponseDTO sellerListResponse = new SellerListResponseDTO(
                sellers,
                sellerPage.getTotalElements(),
                sellerPage.getTotalPages(),
                sellerPage.getNumber()
        );


        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message(sellers.isEmpty() ? "No sellers found" : "Sellers retrieved successfully")
                .data(sellerListResponse)
                .build();
    }

    @Override
    public ResponseDTO activateUser(Long userId) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (currentUser.getId().equals(userId)) {
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Admin account already activated")
                    .build();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));


        if (Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("User account is already activated")
                    .build();
        }

        user.setIsActive(true);
        userRepository.save(user);

        emailService.sendAccountActivatedEmail(user.getEmail());

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("User account activated successfully")
                .build();

    }

    @Override
    public ResponseDTO deactivateUser(Long userId) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (currentUser.getId().equals(userId)) {
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Admin cannot deactivate their own account")
                    .build();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("User account is already deactivated")
                    .build();
        }

        user.setIsActive(false);
        userRepository.save(user);
        tokenService.revokeAllTokens(user.getId());
        emailService.sendAccountDeactivatedEmail(user.getEmail());
        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("User account deactivated successfully")
                .build();
    }
}

