package com.onboardguard.candidate.service.impl;

import com.onboardguard.auth.entity.AppUser;
import com.onboardguard.auth.repository.AppUserRepository;
import com.onboardguard.candidate.dto.request.PersonalDetailsRequestDto;
import com.onboardguard.candidate.dto.request.ProfessionalDetailsRequestDto;
import com.onboardguard.candidate.dto.response.CandidateStatusResponseDto;
import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.enums.CandidateType;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.candidate.mapper.CandidateMapper;
import com.onboardguard.candidate.repository.CandidateRepository;
import com.onboardguard.candidate.service.CandidateService;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final AppUserRepository userRepository;
    private final CandidateMapper candidateMapper;

    private Candidate getOrCreateCandidate(Long userId) {

        return candidateRepository.findByUserId(userId)
                .orElseGet(() -> {

                    AppUser user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("User not found with id: " + userId)
                            );

                    Candidate candidate = Candidate.builder()
                            .user(user)
                            .candidateType(CandidateType.EMPLOYEE)
                            .onboardingStatus(OnboardingStatus.REGISTERED)
                            .build();

                    return candidateRepository.save(candidate);
                });
    }

    private void ensureProfileNotSubmitted(Candidate candidate) {
        if (candidate.getOnboardingStatus() == OnboardingStatus.FORM_SUBMITTED ||
            candidate.getFormSubmittedAt() != null) {
            throw new BadRequestException("Profile has already been submitted and cannot be modified.");
        }
    }

    @Override
    public void savePersonalDetails(Long userId, PersonalDetailsRequestDto dto) {
        Candidate candidate = getOrCreateCandidate(userId);
        ensureProfileNotSubmitted(candidate);

//        if ()
    }

    @Override
    public void saveProfessionalDetails(Long userId, ProfessionalDetailsRequestDto dto) {

    }

    @Override
    public void submitProfile(Long userId) {

    }

    @Override
    public CandidateStatusResponseDto getStatus(Long userId) {
        return null;
    }
}
