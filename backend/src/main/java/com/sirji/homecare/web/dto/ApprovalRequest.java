package com.sirji.homecare.web.dto;

import jakarta.validation.constraints.Size;

public record ApprovalRequest(
    boolean approved,
    @Size(max = 2000) String notes
) {}
