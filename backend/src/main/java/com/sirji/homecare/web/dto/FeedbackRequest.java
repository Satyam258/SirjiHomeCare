package com.sirji.homecare.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
    @NotBlank @Size(max = 160) String customerName,
    @Email @Size(max = 254) String email,
    @Min(1) @Max(5) int rating,
    @NotBlank @Size(max = 4000) String message
) {}
