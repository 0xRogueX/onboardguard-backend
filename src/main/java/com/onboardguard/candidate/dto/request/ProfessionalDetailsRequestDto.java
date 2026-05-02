package com.onboardguard.candidate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProfessionalDetailsRequestDto(
        @NotBlank String currentOrganization,
        String cinNumber,
        String dinNumber,
        @NotBlank String currentDesignation,
        @NotNull BigDecimal totalExperienceYears,
        String previousOrganization,
        String previousDesignation,
        String vendorCompanyName,
        String vendorGstNumber,
        @NotBlank String highestQualification,
        @NotBlank String universityName,
        @NotNull Integer graduationYear
) {}