package com.bootcamp.ecommerce.filters;

import com.bootcamp.ecommerce.CustomUserDetails;
import com.bootcamp.ecommerce.DTO.LoginRequestDTO;
import com.bootcamp.ecommerce.DTO.LoginResponseDTO;
import com.bootcamp.ecommerce.entity.RefreshToken;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.repository.RefreshTokenRepository;
import com.bootcamp.ecommerce.repository.UserRepository;
import com.bootcamp.ecommerce.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;


public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
      RefreshTokenRepository refreshTokenRepository;

    public CustomAuthenticationFilter(UserRepository userRepository, JwtTokenService jwtTokenService) {

        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        try {
            LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

            request.setAttribute("LOGIN_EMAIL", loginRequest.getEmail());

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() ->
                            new BadCredentialsException("Invalid email or password"));

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                throw new LockedException("Account locked due to multiple failed login attempts");
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new DisabledException("Account not activated");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    );

            return this.getAuthenticationManager().authenticate(authToken);

        } catch (AuthenticationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AuthenticationServiceException("Invalid login request", ex);
        }
    }



    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        User user = userDetails.getUser();

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

        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                loginResponse
        );
    }
}
