package com.sirji.homecare.service;

import com.sirji.homecare.domain.AdminUser;
import com.sirji.homecare.web.dto.AuthResponse;
import com.sirji.homecare.web.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        @Value("${app.jwt.expiration-ms:900000}") long expirationMs
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        AdminUser user = (AdminUser) authentication.getPrincipal();
        Instant expiresAt = Instant.now().plusMillis(expirationMs);
        return new AuthResponse(jwtService.createToken(user, expiresAt), "Bearer", user.getRole().name(), expiresAt);
    }
}
