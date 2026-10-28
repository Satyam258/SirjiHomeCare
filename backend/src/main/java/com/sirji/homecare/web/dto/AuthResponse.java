package com.sirji.homecare.web.dto;

import java.time.Instant;

public record AuthResponse(String token, String tokenType, String role, Instant expiresAt) {}
