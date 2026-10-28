package com.sirji.homecare.web.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(UUID id, String customerName, int rating, String message, Instant createdAt) {}
