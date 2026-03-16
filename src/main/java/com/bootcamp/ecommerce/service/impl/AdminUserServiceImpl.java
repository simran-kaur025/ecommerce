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
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    private final EmailService emailService;

    @Override
    public CustomerListResponseDTO getAllCustomers(int pageSize,int offSet, String customSort,String email) {

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
    public SellerListResponseDTO getAllSellers(int pageSize,int offSet, String customSort,String email) {

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
    public ResponseDTO activateUser(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));


        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("User account is already activated");
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

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("User account is already deactivated");
        }

        user.setIsActive(false);
        userRepository.save(user);
        emailService.sendAccountDeactivatedEmail(user.getEmail());
        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("User account deactivated successfully")
                .build();
    }
}

