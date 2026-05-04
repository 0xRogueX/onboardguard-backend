package com.onboardguard.screening.service;

import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.entity.ScreeningResult;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import com.onboardguard.screening.mapper.ScreeningMapper;
import com.onboardguard.screening.repository.ScreeningMatchRepository;
import com.onboardguard.screening.repository.ScreeningResultRepository;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningQueryServiceTest {

    @Mock
    private ScreeningResultRepository screeningResultRepository;

    @Mock
    private ScreeningMatchRepository screeningMatchRepository;

    @Mock
    private ScreeningMapper screeningMapper;

    @InjectMocks
    private ScreeningQueryService screeningQueryService;

    @Test
    void getHistoryShouldReturnMappedSummaryDtos() {
        ScreeningResult result = screeningResult(1L, RiskLevel.MEDIUM, ScreeningStatus.FLAGGED);
        List<ScreeningResult> results = List.of(result);
        List<ScreeningResultDto> mapped = List.of(ScreeningResultDto.builder().screeningResultId(1L).build());

        when(screeningResultRepository.findByCandidateIdOrderByCreatedAtDesc(99L)).thenReturn(results);
        when(screeningMapper.toScreeningResultDtoSummaryList(results)).thenReturn(mapped);

        List<ScreeningResultDto> response = screeningQueryService.getHistory(99L);

        assertEquals(mapped, response);
        verify(screeningResultRepository).findByCandidateIdOrderByCreatedAtDesc(99L);
        verify(screeningMapper).toScreeningResultDtoSummaryList(results);
    }

    @Test
    void getLatestShouldReturnMappedLatestResult() {
        ScreeningResult result = screeningResult(7L, RiskLevel.HIGH, ScreeningStatus.FLAGGED);
        ScreeningResultDto dto = ScreeningResultDto.builder().screeningResultId(7L).build();

        when(screeningResultRepository.findLatestWithMatches(55L)).thenReturn(Optional.of(result));
        when(screeningMapper.toScreeningResultDto(result)).thenReturn(dto);

        ScreeningResultDto response = screeningQueryService.getLatest(55L);

        assertEquals(dto, response);
        verify(screeningResultRepository).findLatestWithMatches(55L);
        verify(screeningMapper).toScreeningResultDto(result);
    }

    @Test
    void getLatestShouldThrowWhenNoResultExists() {
        when(screeningResultRepository.findLatestWithMatches(123L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> screeningQueryService.getLatest(123L));

        assertTrue(ex.getMessage().contains("No screening result found for candidate: 123"));
    }

    @Test
    void getMatchDetailsShouldReturnMappedMatches() {
        List<MatchDetailDto> matches = List.of(MatchDetailDto.builder().watchlistEntryId(1L).build());
        when(screeningMatchRepository.findByScreeningResultId(12L)).thenReturn(List.of());
        when(screeningMapper.toMatchDetailDtos(List.of())).thenReturn(matches);

        List<MatchDetailDto> response = screeningQueryService.getMatchDetails(12L);

        assertEquals(matches, response);
        verify(screeningMatchRepository).findByScreeningResultId(12L);
        verify(screeningMapper).toMatchDetailDtos(List.of());
    }

    private ScreeningResult screeningResult(Long id, RiskLevel level, ScreeningStatus status) {
        Candidate candidate = Candidate.builder()
                .onboardingStatus(OnboardingStatus.REGISTERED)
                .build();
        candidate.setId(id);

        ScreeningResult result = ScreeningResult.builder()
                .candidate(candidate)
                .strategyUsed("BASIC")
                .riskScore(55.0)
                .riskLevel(level)
                .status(status)
                .screeningStartedAt(Instant.parse("2024-01-01T10:00:00Z"))
                .screeningCompletedAt(Instant.parse("2024-01-01T10:01:00Z"))
                .build();
        result.setId(id);
        return result;
    }
}

