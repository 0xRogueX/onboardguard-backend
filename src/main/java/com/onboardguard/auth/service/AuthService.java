package com.onboardguard.auth.service;

import com.onboardguard.auth.dto.request.CreateOfficerDto;
import com.onboardguard.auth.dto.request.LoginRequestDto;
import com.onboardguard.auth.dto.request.RegisterCandidateDto;
import com.onboardguard.auth.dto.response.CandidateLoginResponseDto;
import com.onboardguard.auth.dto.response.StaffLoginResponseDto;

public interface AuthService {

    CandidateLoginResponseDto loginCandidate(LoginRequestDto dto);

    StaffLoginResponseDto loginStaff(LoginRequestDto dto);

    CandidateLoginResponseDto registerCandidate(RegisterCandidateDto dto);

    void createOfficer(CreateOfficerDto dto);

    void logout(String authHeader);
}