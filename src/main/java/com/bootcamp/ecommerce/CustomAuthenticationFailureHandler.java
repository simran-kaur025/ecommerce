package com.bootcamp.ecommerce;

import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (exception instanceof LockedException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(
                    "{\"message\":\"Your account is locked due to multiple failed login attempts\"}"
            );
            return;
        }

        if (exception instanceof DisabledException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(
                    "{\"message\":\"Account not activated. Please check your email.\"}"
            );
            return;
        }

        String email = (String) request.getAttribute("LOGIN_EMAIL");

        if (email != null) {
            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        int attempts =
                                user.getInvalidAttemptCount() == null
                                        ? 1
                                        : user.getInvalidAttemptCount() + 1;

                        user.setInvalidAttemptCount(attempts);

                        if (attempts >= 3) {
                            user.setIsLocked(true);
                        }

                        userRepository.save(user);
                    });
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(
                "{\"message\":\"Invalid email or password\"}"
        );
    }



}

