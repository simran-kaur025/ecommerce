package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileUpdateRequestDTO;
import com.bootcamp.ecommerce.DTO.UserValidationDTO;
import com.bootcamp.ecommerce.SecurityUtils;
import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.exceptionalHandler.ValidationException;
import com.bootcamp.ecommerce.repository.AddressRepository;
import com.bootcamp.ecommerce.repository.SellerRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerProfileServiceImpl implements SellerProfileService {

    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public SellerProfileResponseDTO getMyProfile() {

        User user = SecurityUtils.getCurrentUser();

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        Address address = addressRepository.findByUser(user).orElse(null);

        return SellerProfileResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isActive(user.getIsActive())

                .companyName(seller.getCompanyName())
                .companyContact(seller.getCompanyContact())
                .gst(seller.getGst())
                .address(mapAddress(address))
                .build();


    }

    private AddressDTO mapAddress(Address address) {

        if (address == null) {
            return null;
        }

        return AddressDTO.builder()
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .build();
    }

    @Transactional
    @Override
    public void updateSellerProfile(SellerProfileUpdateRequestDTO request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getMiddleName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getCompanyContact() != null) {

            boolean exists = sellerRepository.existsByCompanyContactAndIdNot(request.getCompanyContact(), seller.getId());

            if (exists) {
                throw new ValidationException(
                        List.of(
                                UserValidationDTO.builder()
                                        .key("contact")
                                        .errors(List.of("Phone number already exists"))
                                        .build()
                        )
                );

            }

            seller.setCompanyContact(request.getCompanyContact());
        }

        if (request.getAddress() != null) {

            Address address = addressRepository.findByUser(user)
                    .orElse(new Address());

            address.setUser(user);

            if (request.getAddress().getAddressLine() != null) {
                address.setAddressLine(request.getAddress().getAddressLine());
            }

            if (request.getAddress().getCity() != null) {
                address.setCity(request.getAddress().getCity());
            }

            if (request.getAddress().getState() != null) {
                address.setState(request.getAddress().getState());
            }

            if (request.getAddress().getCountry() != null) {
                address.setCountry(request.getAddress().getCountry());
            }

            if (request.getAddress().getZipCode() != null) {
                address.setZipCode(request.getAddress().getZipCode());
            }

            addressRepository.save(address);
        }


        userRepository.save(user);
        sellerRepository.save(seller);
    }

}
