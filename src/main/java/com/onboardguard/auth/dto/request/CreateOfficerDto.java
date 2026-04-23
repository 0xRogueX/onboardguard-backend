package com.onboardguard.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOfficerDto(

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Department is required")
        String department,

        String phone
) {}