package com.onboardguard.screening.dto;

import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * The complete output of a screening run.
 * Returned from the strategy, persisted by ScreeningOrchestrationService,
 * and also returned to the caller (e.g. controller, event listener).
 */
@Builder
public record ScreeningResultDto (
        Long   screeningResultId,
        Long   candidateId,
        String strategyUsed,

        Double riskScore,
        RiskLevel riskLevel,
        ScreeningStatus status,

        List<MatchDetailDto> matches,

        // How many entries were checked across all watchlist categories
        int totalEntriesChecked,

        Instant screeningStartedAt,
        Instant screeningCompletedAt
) { }