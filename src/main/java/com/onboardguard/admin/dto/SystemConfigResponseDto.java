package com.onboardguard.admin.dto;

import lombok.Data;

@Data
public class SystemConfigResponseDto {
    private Long id;
    private String configKey;  // e.g., MAX_LOGIN_ATTEMPTS, EXTERNAL_API_KEY
    private String configValue;
    private String configType; // e.g., SECURITY, INTEGRATION, THRESHOLD
    private String description;
    private Boolean isSensitive; // If true, frontend should display "********"
}