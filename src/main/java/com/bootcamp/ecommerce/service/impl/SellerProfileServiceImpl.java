package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.SellerProfileResponseDTO;
import com.bootcamp.ecommerce.SecurityUtils;
import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.Seller;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.AddressRepository;
import com.bootcamp.ecommerce.repository.SellerRepository;
import com.bootcamp.ecommerce.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerProfileServiceImpl implements SellerProfileService {

    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    @Override
    public SellerProfileResponseDTO getMyProfile() {

        User user = SecurityUtils.getCurrentUser();

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Seller profile not found"));

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
}
