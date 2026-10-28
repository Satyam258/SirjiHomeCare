package com.sirji.homecare.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettingUpdateRequest(@NotBlank @Size(max = 500) String value) {}
