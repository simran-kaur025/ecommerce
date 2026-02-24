    package com.bootcamp.ecommerce.service.impl;

    import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
    import com.bootcamp.ecommerce.DTO.ResponseDTO;
    import com.bootcamp.ecommerce.DTO.SellerRequestDTO;
    import com.bootcamp.ecommerce.DTO.UserValidationDTO;
    import com.bootcamp.ecommerce.constant.Constant;
    import com.bootcamp.ecommerce.entity.*;
    import com.bootcamp.ecommerce.exceptionalHandler.InvalidOperationException;
    import com.bootcamp.ecommerce.exceptionalHandler.ResourceNotFoundException;
    import com.bootcamp.ecommerce.exceptionalHandler.ValidationException;
    import com.bootcamp.ecommerce.repository.*;
    import com.bootcamp.ecommerce.service.ActivationTokenService;
    import com.bootcamp.ecommerce.entity.ActivationToken;
    import com.bootcamp.ecommerce.service.EmailService;
    import com.bootcamp.ecommerce.service.RegisterService;
    import com.bootcamp.ecommerce.service.RegisterValidationService;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class RegisterServiceImpl implements RegisterService {

        private final RegisterValidationService registerValidationService;
        private final UserRepository userRepository;
        private final CustomerRepository customerRepository;
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;
        private final SellerRepository sellerRepository;
        private final ActivationTokenRepository activationTokenRepository;
        private final  PasswordEncoder passwordEncoder;
        private final AddressRepository addressRepository;

       private final ActivationTokenService activationTokenService;
       private final EmailService emailService;


        @Transactional
        @Override
        public ResponseDTO registerCustomer(CustomerRequestDTO requestDTO) {

            List<UserValidationDTO> errors = new ArrayList<>();
            registerValidationService.validateCustomer(requestDTO, errors);

            if (!errors.isEmpty()) {
                log.error("Customer registration validation failed for email: {}", requestDTO.getEmail());
                throw new ValidationException(errors);
            }

            User user = createUser(requestDTO);

            Role role = roleRepository.findByAuthority("ROLE_CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));


            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);

            Customer customer = new Customer();
            customer.setUser(user);
            customer.setContact(requestDTO.getPhoneNumber());
            customerRepository.save(customer);

            ActivationToken activationToken = activationTokenService.createToken(user);
            if (activationToken == null || activationToken.getToken() == null) {
                throw new IllegalStateException("Activation token creation failed");
            }
            try {
                emailService.sendActivationEmail(user.getEmail(), activationToken.getToken()
                );
            } catch (Exception ex) {
                log.error("Failed to send activation email for user {}", user.getEmail(), ex);
            }
            log.info("Customer registered successfully for email: {}", user.getEmail());

            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Customer registered successfully. Please check your email to activate your account.")
                    .build();
        }


        @Transactional
        @Override
        public ResponseDTO registerSeller(SellerRequestDTO requestDTO) {

            List<UserValidationDTO> errors = new ArrayList<>();
            registerValidationService.validateSeller(requestDTO, errors);

            if (!errors.isEmpty()) {
                log.error("Seller registration validation failed for email: {}", requestDTO.getEmail());
                throw new ValidationException(errors);
            }

            User user = new User();
            user.setEmail(requestDTO.getEmail());
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
            user.setFirstName(requestDTO.getFirstName());
            user.setMiddleName(requestDTO.getMiddleName());
            user.setLastName(requestDTO.getLastName());
            user.setIsActive(false);
            user.setIsDeleted(false);

            userRepository.save(user);

            Role role = roleRepository.findByAuthority("ROLE_SELLER")
                    .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));


            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);

            Seller seller = new Seller();
            seller.setUser(user);
            seller.setGst(requestDTO.getGst());
            seller.setCompanyName(requestDTO.getCompanyName());
            seller.setCompanyContact(requestDTO.getCompanyContact());
            sellerRepository.save(seller);

            if (requestDTO.getAddress() != null) {

                Address address = new Address();
                address.setAddressLine(requestDTO.getAddress().getAddressLine());
                address.setCity(requestDTO.getAddress().getCity());
                address.setState(requestDTO.getAddress().getState());
                address.setCountry(requestDTO.getAddress().getCountry());
                address.setZipCode(requestDTO.getAddress().getZipCode());
                address.setUser(user);

                addressRepository.save(address);
            }



            emailService.sendSellerRegistrationEmail(requestDTO.getEmail());

            log.info("Seller registered successfully and awaiting admin approval: {}", user.getEmail());
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Seller registered successfully. Awaiting admin approval.")
                    .build();
        }

        @Override
        public ResponseDTO activateAccount(String token) {

            ActivationToken activationToken = activationTokenRepository.findByToken(token)
                            .orElseThrow(() -> {
                                log.warn("Invalid activation token received");
                                return new ResourceNotFoundException("Invalid activation token");
                            }
                            );

            if (activationToken.getExpiryTime().isBefore(LocalDateTime.now())) {

                User user = activationToken.getUser();

                activationTokenRepository.delete(activationToken);

                ActivationToken newToken = activationTokenService.createToken(user);

                emailService.sendActivationEmail(user.getEmail(), newToken.getToken());

                log.warn("Activation token expired. New token sent to {}", user.getEmail());

                throw new InvalidOperationException("Activation token expired. A new activation link has been sent to your email.");
            }

            if (Boolean.TRUE.equals(activationToken.getUser().getIsActive())) {
                throw new InvalidOperationException("Account is already activated");
            }

            User user = activationToken.getUser();
            user.setIsActive(true);
            userRepository.save(user);

            activationTokenRepository.delete(activationToken);

            log.info("Account activated successfully for user: {}", user.getEmail());
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Account activated successfully")
                    .build();
        }


        public ResponseDTO resendActivationToken(String email) {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found with email: " + email)
                    );

            if (Boolean.TRUE.equals(user.getIsActive())) {
                log.warn("Resend activation attempted for already active account");
                throw new InvalidOperationException("Account is already activated");
            }


            activationTokenService.deleteOldToken(email);
            ActivationToken activationToken= activationTokenService.createToken(user);

            if (activationToken == null || activationToken.getToken() == null) {
                log.error("Activation token creation failed during resend");
                throw new IllegalStateException("Activation token creation failed");
            }
            try {
                emailService.sendActivationEmail(user.getEmail(), activationToken.getToken()
                );
            } catch (Exception ex) {
                log.error("Failed to send activation email for user {}", user.getEmail(), ex);
            }
            log.info("Activation email resent successfully");
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Activation email resent successfully")
                    .build();
        }



        private User createUser(CustomerRequestDTO requestDTO) {

            User user = new User();
            user.setEmail(requestDTO.getEmail());
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
            user.setFirstName(requestDTO.getFirstName());
            user.setMiddleName(requestDTO.getMiddleName());
            user.setLastName(requestDTO.getLastName());
            user.setIsActive(false);
            user.setIsDeleted(false);

            userRepository.saveAndFlush(user);

            return user;
        }

    }
