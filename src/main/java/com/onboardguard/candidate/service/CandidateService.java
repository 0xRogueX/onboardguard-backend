package com.onboardguard.candidate.service;

import com.onboardguard.candidate.dto.request.PersonalDetailsRequestDto;
import com.onboardguard.candidate.dto.request.ProfessionalDetailsRequestDto;
import com.onboardguard.candidate.dto.response.CandidateStatusResponseDto;

public interface CandidateService {

    void savePersonalDetails(Long userId, PersonalDetailsRequestDto dto);

    void saveProfessionalDetails(Long userId, ProfessionalDetailsRequestDto dto);

    void submitProfile(Long userId);

    CandidateStatusResponseDto getStatus(Long userId);
}
