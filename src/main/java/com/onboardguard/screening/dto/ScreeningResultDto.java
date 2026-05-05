package com.onboardguard.screening.dto;

import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;


@Getter
@Builder
public class ScreeningResultDto {

    private Long   screeningResultId;
    private Long   candidateId;
    private String strategyUsed;

    private Double riskScore;
    private RiskLevel riskLevel;
    private ScreeningStatus status;

    private List<MatchDetailDto> matches;

    // How many entries were checked across all watchlist categories
    private int totalEntriesChecked;

    private Instant screeningStartedAt;
    private Instant screeningCompletedAt;
}