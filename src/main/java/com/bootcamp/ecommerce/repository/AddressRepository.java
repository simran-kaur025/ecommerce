package com.bootcamp.ecommerce.repository;

import com.bootcamp.ecommerce.entity.Address;
import com.bootcamp.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByUser(User user);
    Optional<Address> findByIdAndUser(Long addressId, User user);
    boolean existsByUserAndAddressLineIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndCountryIgnoreCaseAndZipCodeIgnoreCase(
            User user,
            String addressLine,
            String city,
            String state,
            String country,
            String zipCode
    );
}

