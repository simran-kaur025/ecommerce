package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.CustomUserDetails;
import com.bootcamp.ecommerce.DTO.ChangePasswordRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.ForgotPasswordToken;
import com.bootcamp.ecommerce.entity.RefreshToken;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.repository.ForgotPasswordTokenRepository;
import com.bootcamp.ecommerce.repository.RefreshTokenRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AuthService;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ResponseDTO login(LoginRequestDTO requestDTO) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDTO.getEmail(),
                            requestDTO.getPassword()
                    )
            );

            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();

            User user = userDetails.getUser();

            if (!user.getIsActive()) {
                return ResponseDTO.builder()
                        .status(Constant.FAIL)
                        .message("Account not activated")
                        .build();
            }

            user.setInvalidAttemptCount(0);
            userRepository.save(user);

            String accessToken = jwtTokenService.generateAccessToken(user);
            String refreshToken = jwtTokenService.generateRefreshToken(user);

            RefreshToken tokenEntity = new RefreshToken();
            tokenEntity.setToken(refreshToken);
            tokenEntity.setUser(user);
            tokenEntity.setExpiryDate(
                    jwtTokenService.getRefreshTokenExpiryDate()
            );

            refreshTokenRepository.save(tokenEntity);
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .data(
                            LoginResponseDTO.builder()
                                    .accessToken(accessToken)
                                    .refreshToken(refreshToken)
                                    .build()
                    )
                    .build();

        } catch (BadCredentialsException ex) {

            User user = userRepository.findByEmail(requestDTO.getEmail()).orElse(null);

            if (user != null) {
                user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

                if (user.getInvalidAttemptCount() >= 3) {
                    user.setIsLocked(true);
                }
                userRepository.save(user);
            }

            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Invalid email or password")
                    .build();
        }
    }
    @Override
    public ResponseDTO logout(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if (token == null) {
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Invalid refresh token")
                    .build();
        }

        if (token.getExpiryDate().before(new Date())) {

            log.warn("Logout attempt with expired refresh token for userId={}", token.getUser().getId());

            refreshTokenRepository.delete(token);

            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Refresh token already expired")
                    .build();
        }
        refreshTokenRepository.delete(token);


        log.warn("Logout attempt with expired refresh token for userId={}", token.getUser().getId()
        );

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Logged out successfully")
                .build();
    }

    @Override
    public ResponseDTO  refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() ->
                        new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().before(new Date())) {

            refreshTokenRepository.delete(token);

            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Refresh token expired")
                    .build();
        }

        User user = token.getUser();
        String newAccessToken = jwtTokenService.generateAccessToken(user);

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .data(
                        Map.of("accessToken", newAccessToken)
                )
                .build();
    }

    @Transactional
    @Override
    public ResponseDTO forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        if (!user.getIsActive()) {
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Account is not activated")
                    .build();
        }

        forgotPasswordTokenRepository.deleteByUser(user);
        forgotPasswordTokenRepository.flush();


        String token = UUID.randomUUID().toString();

        ForgotPasswordToken resetToken = new ForgotPasswordToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(
                new Date(System.currentTimeMillis() + 15 * 60 * 1000)
        );

        forgotPasswordTokenRepository.save(resetToken);

        emailService.sendForgotPasswordEmail(
                user.getEmail(), token);

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Password reset link sent to your email")
                .build();
    }


    @Transactional
    @Override
    public ResponseDTO resetPassword(String token, String password, String confirmPassword) {

        ForgotPasswordToken resetToken = forgotPasswordTokenRepository.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().before(new Date())) {
            forgotPasswordTokenRepository.delete(resetToken);
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Token expired")
                    .build();
        }

        if (!password.equals(confirmPassword)) {
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Passwords do not match")
                    .build();
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        forgotPasswordTokenRepository.delete(resetToken);
        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Password reset successfully")
                .build();
    }


    @Transactional
    @Override
    public void changePassword(ChangePasswordRequestDTO request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from old password");
        }

        if (!isStrongPassword(request.getNewPassword())) {
            throw new RuntimeException(
                    "Password must contain uppercase, lowercase, number, special character and be 8-15 characters long"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangeEmail(user.getEmail());
    }


    private boolean isStrongPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$"
        );
    }


}

