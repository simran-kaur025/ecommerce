package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.CustomUserDetails;
import com.bootcamp.ecommerce.DTO.ChangePasswordRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.AccessToken;
import com.bootcamp.ecommerce.entity.ForgotPasswordToken;
import com.bootcamp.ecommerce.entity.RefreshToken;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.repository.AccessTokenRepository;
import com.bootcamp.ecommerce.repository.ForgotPasswordTokenRepository;
import com.bootcamp.ecommerce.repository.RefreshTokenRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AuthService;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MessageSource messageSource;

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
    @Transactional
    public ResponseDTO logout(String accessTokenValue) {

        AccessToken accessToken = accessTokenRepository
                .findByToken(accessTokenValue).orElseThrow(() -> new RuntimeException("Invalid access token"));

        if (!Integer.valueOf(1).equals(accessToken.getStatus())) {
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Token already revoked or inactive")
                    .build();
        }

        if (accessToken == null) {
            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Invalid access token")
                    .build();
        }

        if (accessToken.getExpiryDate().before(new Date())) {

            log.warn("Logout attempt with expired access token for userId={}",
                    accessToken.getUser().getId());

            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Access token already expired")
                    .build();
        }

        User user = accessToken.getUser();

        RefreshToken refreshToken = refreshTokenRepository
                .findByUserAndStatus(user,1)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        accessToken.setStatus(2);
        refreshToken.setStatus(2);

        accessTokenRepository.save(accessToken);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged out successfully for userId={}", user.getId());

        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Logged out successfully")
                .build();
    }


    @Override
    public ResponseDTO  refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() ->
                        new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().before(new Date())) {

            return ResponseDTO.builder()
                    .status(Constant.FAIL)
                    .message("Refresh token expired")
                    .build();
        }

        User user = token.getUser();
        String newAccessTokenValue = jwtTokenService.generateAccessToken(user);

        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setToken(newAccessTokenValue);
        newAccessToken.setUser(user);
        newAccessToken.setStatus(1);
        newAccessToken.setExpiryDate(jwtTokenService.getAccessTokenExpiryDate());

        accessTokenRepository.save(newAccessToken);

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
        if (!isStrongPassword(password)) {
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number, special character and be 8-15 characters long"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);

        forgotPasswordTokenRepository.delete(resetToken);
        return ResponseDTO.builder()
                .status(Constant.SUCCESS)
                .message("Password reset successfully")
                .build();
    }


    @Transactional
    @Override
    public void changePassword(ChangePasswordRequestDTO request, Locale locale) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            String msg = messageSource.getMessage("error.password.mismatch", null, locale);
            throw new BadRequestException(msg);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        if (!isStrongPassword(request.getNewPassword())) {
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number, special character and be 8-15 characters long"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendPasswordChangeEmail(user.getEmail());
    }


    private boolean isStrongPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$"
        );
    }


}

