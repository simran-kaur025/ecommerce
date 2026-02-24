package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.UpdateAddressRequestDTO;
import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.repository.AddressRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public void updateAddress(Long addressId, UpdateAddressRequestDTO request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email={}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Address address = addressRepository
                .findByIdAndUser(addressId, user)
                .orElseThrow(() -> {
                    log.error("Address not found. addressId={}, userId={}", addressId, user.getId());
                    return new ResourceNotFoundException("Address not found");
                });

        if (request.getAddressLine() == null &&
                request.getCity() == null &&
                request.getState() == null &&
                request.getZipCode() == null &&
                request.getCountry() == null &&
                request.getLabel() == null) {

            throw new IllegalArgumentException("At least one field must be provided for update");
        }


        if (request.getAddressLine() != null) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if(request.getLabel()!= null){
            address.setLabel(request.getLabel());
        }

        addressRepository.save(address);
        log.info("Address updated successfully. addressId={}, userId={}",
                addressId, user.getId());
    }

}
