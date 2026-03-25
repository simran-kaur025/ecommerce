package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.entity.AccessToken;
import com.bootcamp.ecommerce.entity.Role;
import com.bootcamp.ecommerce.entity.User;
import com.bootcamp.ecommerce.entity.UserRole;
import com.bootcamp.ecommerce.repository.AccessTokenRepository;
import com.bootcamp.ecommerce.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private final AccessTokenRepository accessTokenRepository;


    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getAuthority)
                .toList();

        claims.put("roles", roles);


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Date getAccessTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + accessTokenExpiry);
    }

    @Override
    public String generateRefreshToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getAuthority)
                .toList();

        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }


    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + refreshTokenExpiry);
    }




    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token) &&isAccessTokenActive(token) ;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public boolean isAccessTokenActive(String token) {

        return accessTokenRepository.findByToken(token)
                .map(t -> Integer.valueOf(1).equals(t.getStatus()))
                .orElse(false);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {

        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

}
