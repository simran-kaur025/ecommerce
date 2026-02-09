package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.InvalidOperationException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.CustomerRepository;
import com.bootcamp.ecommerce.repository.SellerRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AdminUserService;
import com.bootcamp.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    EmailService emailService;
    @Override
    public CustomerListResponseDTO getAllCustomers(AdminUserSearchRequestDTO request) {

        Pageable pageable = PageRequest.of(
                request.getPageOffset(),
                request.getPageSize(),
                Sort.by(request.getSort()).ascending()
        );

        Page<Customer> userPage;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            userPage = customerRepository
                    .findByUserEmailContainingIgnoreCase(
                            request.getEmail(), pageable);
        } else {
            userPage = customerRepository.findAll(pageable);
        }
        if (userPage.isEmpty()) {
            throw new ResourceNotFoundException("No customers found");
        }

        List<CustomerResponse> customers =
                userPage.getContent().stream()
                        .map(customer -> new CustomerResponse(
                                customer.getUser().getId(),
                                customer.getUser().getFirstName() + " " + customer.getUser().getLastName(),
                                customer.getUser().getEmail(),
                                customer.getUser().getIsActive()
                        ))
                        .toList();

        return new CustomerListResponseDTO(
                customers,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber()
        );
    }

    @Override
    public SellerListResponseDTO getAllSellers(AdminUserSearchRequestDTO request) {

        Pageable pageable = PageRequest.of(
                request.getPageOffset(),
                request.getPageSize(),
                Sort.by(request.getSort()).ascending()
        );

        Page<Seller> sellerPage;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            sellerPage = sellerRepository
                    .findByUserEmailContainingIgnoreCase(
                            request.getEmail(), pageable);
        } else {
            sellerPage = sellerRepository.findAll(pageable);
        }

        if (sellerPage.isEmpty()) {
            throw new ResourceNotFoundException("No Sellers found");
        }
        List<SellerResponse> sellers =
                sellerPage.getContent().stream()
                        .map(seller -> new SellerResponse(
                                seller.getUser().getId(),
                                seller.getUser().getFirstName() + " " + seller.getUser().getLastName(),
                                seller.getUser().getEmail(),
                                seller.getUser().getIsActive(),
                                seller.getCompanyName(),
                                seller.getCompanyContact()
                        ))
                        .toList();


        return new SellerListResponseDTO(
                sellers,
                sellerPage.getTotalElements(),
                sellerPage.getTotalPages(),
                sellerPage.getNumber()
        );
    }

    @Override
    public ResponseDTO activateCustomer(UUID customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found with id: " + customerId)
                );

        User user = customer.getUser();

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("Customer account is already activated");
        }

        user.setIsActive(true);
        userRepository.save(user);

        //emailService.sendAccountActivatedEmail(user.getEmail());

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Customer account activated successfully")
                .build();

    }

    @Override
    public ResponseDTO deactivateCustomer(UUID customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found with id: " + customerId)
                );

        User user = customer.getUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("Customer account is already deactivated");
        }

        user.setIsActive(false);
        userRepository.save(user);
        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Customer account deactivated successfully")
                .build();
    }
}

