package com.onboardguard.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onboardguard.shared.common.enums.RoleCode;
import java.time.Instant;

public record UserResponseDto(
        Long id,
        String fullName,
        String email,
        String phone,
        RoleCode role,
        @JsonProperty("isActive") boolean isActive,
        boolean locked,
        Instant lastLoginAt,
        Instant createdAt
) {}