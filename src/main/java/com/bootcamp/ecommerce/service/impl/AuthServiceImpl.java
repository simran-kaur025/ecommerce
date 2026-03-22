package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.ChangePasswordRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginResponseDTO;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.constant.Constant;
import com.bootcamp.ecommerce.entity.AccessToken;
import com.bootcamp.ecommerce.entity.ForgotPasswordToken;
import com.bootcamp.ecommerce.entity.RefreshToken;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.exceptionalHandler.*;
import com.bootcamp.ecommerce.exceptionalHandler.DisabledException;
import com.bootcamp.ecommerce.exceptionalHandler.LockedException;
import com.bootcamp.ecommerce.repository.AccessTokenRepository;
import com.bootcamp.ecommerce.repository.ForgotPasswordTokenRepository;
import com.bootcamp.ecommerce.repository.RefreshTokenRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.AuthService;
import com.bootcamp.ecommerce.service.EmailService;
import com.bootcamp.ecommerce.service.JwtTokenService;
import com.bootcamp.ecommerce.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final TokenService tokenService;

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Value("${password.reset.token.expiry}")
    private long tokenExpiry;


    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new LockedException("Account locked due to multiple failed login attempts");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("Account not activated");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            user.setInvalidAttemptCount(0);
            userRepository.save(user);

        } catch (AuthenticationException ex) {

            int count = user.getInvalidAttemptCount() + 1;
            user.setInvalidAttemptCount(count);

            if (count >= 3) {
                user.setIsLocked(true);
                emailService.sendEmail(user.getEmail(), "Account Locked", "Your account is locked.");
            }

            userRepository.save(user);

            throw new UnauthorizedException("Invalid email or password");
        }

        tokenService.revokeAllTokens(user.getId());

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        AccessToken accessTokenEntity = new AccessToken();
        accessTokenEntity.setToken(accessToken);
        accessTokenEntity.setUser(user);
        accessTokenEntity.setStatus(1);
        accessTokenEntity.setExpiryDate(jwtTokenService.getAccessTokenExpiryDate());

        accessTokenRepository.save(accessTokenEntity);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setStatus(1);
        refreshTokenEntity.setExpiryDate(jwtTokenService.getRefreshTokenExpiryDate());

        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    @Transactional
    public ResponseDTO logout(String accessTokenValue) {

        AccessToken accessToken = accessTokenRepository.findByToken(accessTokenValue).orElseThrow(() -> new InvalidTokenException("Invalid access token"));

        if (!Integer.valueOf(1).equals(accessToken.getStatus())) {
            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Already Logged out")
                    .build();
        }


        if (accessToken.getExpiryDate().before(new Date())) {

            log.warn("Logout attempt with expired access token for userId={}",
                    accessToken.getUser().getId());

            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Access token already expired")
                    .build();
        }

        User user = accessToken.getUser();

        RefreshToken refreshToken = refreshTokenRepository
                .findByUserAndStatus(user,1)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

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
    @Transactional
    public ResponseDTO  refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.getExpiryDate().before(new Date())) {

            return ResponseDTO.builder()
                    .status(Constant.SUCCESS)
                    .message("Refresh token expired")
                    .build();
        }

        User user = token.getUser();

        accessTokenRepository.revokeAllByUser(user.getId());

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
                        Map.of("accessToken", newAccessTokenValue)
                )
                .build();
    }

    @Transactional
    @Override
    public ResponseDTO forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email does not exist"));

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
                new Date(System.currentTimeMillis() + tokenExpiry)
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
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

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
            throw new BadRequestException("Password must contain uppercase, lowercase, number, special character and be 8-15 characters long");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.saveAndFlush(user);

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
        userRepository.saveAndFlush(user);

        emailService.sendPasswordChangeEmail(user.getEmail());
    }


    private boolean isStrongPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$"
        );
    }

}

