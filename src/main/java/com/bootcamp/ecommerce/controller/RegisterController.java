package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.CustomerRequestDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.DTO.SellerRequestDTO;
import com.bootcamp.ecommerce.service.RegisterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/customer")
    public ResponseEntity<ResponseDTO> registerCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {

        log.info("Register request received for email: {}", requestDTO.getEmail());

        ResponseDTO response = registerService.registerCustomer(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/customer/activate")
    public ResponseEntity<ResponseDTO> activateAccount(
            @RequestParam("token") String token) {

        log.info("Account activation request received");
        ResponseDTO response = registerService.activateAccount(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<ResponseDTO> resendActivationToken(
            @RequestParam("email") String email) {
        log.info("Resend activation token request received");
        ResponseDTO response = registerService.resendActivationToken(email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }



    @PostMapping("/seller")
    public ResponseEntity<ResponseDTO> registerSeller(
            @Valid @RequestBody SellerRequestDTO requestDTO) {

        log.info("Seller registration request received");

        ResponseDTO response = registerService.registerSeller(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


}

