package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.*;
import com.bootcamp.ecommerce.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/login")
//    public ResponseDTO login(@RequestBody @Valid LoginRequestDTO requestDTO) {
//        return authService.login(requestDTO);
//    }



    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .badRequest()
                    .body(ResponseDTO.builder()
                            .status("FAIL")
                            .message("Refresh token is missing")
                            .build());
        }

        String refreshToken = authorizationHeader.substring(7);

        ResponseDTO response = authService.logout(refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO> refreshAccessToken(@NotBlank @RequestParam String refreshToken) {

        log.info("Refresh token request received");
        ResponseDTO response = authService.refreshAccessToken(refreshToken);
        log.info("Access token refreshed successfully");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);


    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {

        log.info("Forgot password request received for email: {}", requestDTO.getEmail());

        ResponseDTO response = authService.forgotPassword(requestDTO.getEmail());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {

        log.info("Reset password request received");

        ResponseDTO response = authService.resetPassword(
                requestDTO.getToken(),
                requestDTO.getPassword(),
                requestDTO.getConfirmPassword()
        );

        log.info("Password reset successfully");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
