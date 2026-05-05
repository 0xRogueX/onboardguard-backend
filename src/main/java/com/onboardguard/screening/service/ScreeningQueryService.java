package com.onboardguard.screening.service;

import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.entity.ScreeningResult;
import com.onboardguard.screening.mapper.ScreeningMapper;
import com.onboardguard.screening.repository.ScreeningMatchRepository;
import com.onboardguard.screening.repository.ScreeningResultRepository;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningQueryService {

    private final ScreeningResultRepository screeningResultRepository;
    private final ScreeningMatchRepository screeningMatchRepository;
    private final ScreeningMapper screeningMapper;

    // Full screening history for a candidate - summary DTOs, no match detail.
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALERT_VIEW')")
    public List<ScreeningResultDto> getHistory(Long candidateId) {
        log.debug("Fetching screening history for candidateId={}", candidateId);
        List<ScreeningResult> results = screeningResultRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
        return screeningMapper.toScreeningResultDtoSummaryList(results);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALERT_VIEW')")
    public List<MatchDetailDto> getMatchDetails(Long resultId) {
        log.debug("Fetching match details for screeningResultId={}", resultId);
        return screeningMapper.toMatchDetailDtos(
                screeningMatchRepository.findByScreeningResultId(resultId)
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALERT_VIEW')")
    public ScreeningResultDto getLatest(Long candidateId) {
        log.debug("Fetching latest screening result for candidateId={}", candidateId);

        ScreeningResult result = screeningResultRepository
                .findLatestWithMatches(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No screening result found for candidate: " + candidateId));

        return screeningMapper.toScreeningResultDto(result);
    }
}