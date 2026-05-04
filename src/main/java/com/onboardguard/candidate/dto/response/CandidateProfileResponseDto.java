package com.onboardguard.candidate.dto.response;

import com.onboardguard.candidate.dto.request.PersonalDetailsRequestDto;
import com.onboardguard.candidate.dto.request.ProfessionalDetailsRequestDto;
import com.onboardguard.candidate.enums.OnboardingStatus;

public record CandidateProfileResponseDto(
        PersonalDetailsRequestDto personalDetails,
        ProfessionalDetailsRequestDto professionalDetails,
        OnboardingStatus onboardingStatus
) {}