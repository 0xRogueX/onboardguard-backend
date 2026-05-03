package com.onboardguard.candidate.dto.response;

import com.onboardguard.candidate.enums.CandidateDocumentType;
import com.onboardguard.candidate.enums.CandidateType;
import com.onboardguard.candidate.enums.DocumentStatus;
import com.onboardguard.candidate.enums.OnboardingStatus;

import java.util.Map;

public record CandidateStatusResponseDto(
        CandidateType candidateType,
        OnboardingStatus onboardingStatus,
        boolean isSubmitted,
        Map<CandidateDocumentType, DocumentStatus> documentStatuses
) {}