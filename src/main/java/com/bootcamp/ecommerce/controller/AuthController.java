package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseDTO login(@RequestBody @Valid LoginRequestDTO requestDTO) {
        return authService.login(requestDTO);
    }

    @PostMapping("/logout")
    public ResponseDTO logout(@Valid @RequestBody LogoutRequestDTO requestDTO) {
        return authService.logout(requestDTO);
    }

    @PostMapping("/refresh")
    public ResponseDTO refreshAccessToken(@NotBlank @RequestParam String refreshToken) {

        return authService.refreshAccessToken(refreshToken);
    }

    @PostMapping("/forgot-password")
    public ResponseDTO forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {

        return authService.forgotPassword(requestDTO.getEmail());
    }

    @PutMapping("/reset-password")
    public ResponseDTO resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO requestDTO) {

        return authService.resetPassword(
                requestDTO.getToken(),
                requestDTO.getPassword(),
                requestDTO.getConfirmPassword()
        );
    }
}
