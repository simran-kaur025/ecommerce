package com.bootcamp.ecommerce.service.impl;


import com.bootcamp.ecommerce.CustomUserDetails;
import com.bootcamp.ecommerce.DTO.AddressDTO;
import com.bootcamp.ecommerce.DTO.CustomerProfileResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.UpdateProfileRequestDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.Customer;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.CustomerNotFoundException;
import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
import com.bootcamp.ecommerce.exceptionalHandler.UnauthorizedException;
import com.bootcamp.ecommerce.exceptionalHandler.UserNotFoundException;
import com.bootcamp.ecommerce.repository.AddressRepository;
import com.bootcamp.ecommerce.repository.CustomerRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerProfileResponseDTO getMyProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));


        return CustomerProfileResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .contact(customer.getContact())
//                .image(customer.getImage())

                .build();
    }


    @Override
    public ResponseDTO getMyAddress() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        List<AddressDTO> addresses = addressRepository.findByUser(user)
                        .stream()
                        .map(address -> AddressDTO.builder()
                                .city(address.getCity())
                                .state(address.getState())
                                .country(address.getCountry())
                                .addressLine(address.getAddressLine())
                                .label(address.getLabel())
                                .zipCode(address.getZipCode())
                                .build())
                        .toList();

        if (addresses.isEmpty()) {
        throw new ResourceNotFoundException("No address found for this user");
    }

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .data(addresses)
                .build();
    }

    public void updateProfile(UpdateProfileRequestDTO request) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Update profile attempt without authentication");
            throw new UnauthorizedException("User not authenticated");
        }

        String email = auth.getName();
        log.info("Profile update requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Customer not found for userId: {}", user.getId());
                    return new CustomerNotFoundException("Customer not found");
                });

        if (request.getPhone() != null) {
            log.debug("Updating phone for user {} -> {}", email, request.getPhone());
            customer.setContact(request.getPhone());
        }

        userRepository.save(user);
        customerRepository.save(customer);

        log.info("Profile updated successfully for email: {}", email);
    }


    public void addAddress(AddressDTO request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setLabel(request.getLabel());

        addressRepository.save(address);
    }

    public void deleteAddress(Long addressId) {

        log.info("Delete address request received for addressId={}", addressId);

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email={}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Address address = addressRepository
                .findByIdAndUser(addressId, user)
                .orElseThrow(() -> {
                    log.error("Address not found. addressId={}, userId={}",
                            addressId, user.getId());
                    return new ResourceNotFoundException("Address not found");
                });

        addressRepository.delete(address);
        log.info("Address deleted successfully. addressId={}, userId={}",
                addressId, user.getId());
    }

    public void updateAddress(Long addressId, AddressDTO request) {

        log.info("Update address request received. addressId={}", addressId);

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

        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setLabel(request.getLabel());

        addressRepository.save(address);

        log.info("Address updated successfully. addressId={}, userId={}",
                addressId, user.getId());
    }
}

