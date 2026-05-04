package com.onboardguard.screening.strategy;

import com.onboardguard.screening.dto.CandidateScreeningData;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import com.onboardguard.screening.service.RiskScoringEngine;
import com.onboardguard.screening.util.NameMatchingUtil;
import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.shared.common.enums.SourceType;
import com.onboardguard.watchlist.entity.WatchlistCategory;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.entity.WatchlistSource;
import com.onboardguard.watchlist.repository.WatchlistEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicScreeningStrategyTest {

    @Mock
    private WatchlistEntryRepository watchlistEntryRepository;

    @Mock
    private RiskScoringEngine riskScoringEngine;

    private BasicScreeningStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BasicScreeningStrategy(watchlistEntryRepository, riskScoringEngine, new NameMatchingUtil());
    }

    @Test
    void screenShouldReturnFlaggedResultForExactNameMatch() {
        WatchlistEntry entry = WatchlistEntry.builder()
                .id(11L)
                .primaryName("John Doe").primaryNameNormalized("john doe")
                .category(WatchlistCategory.builder().code(CategoryCode.CRIMINAL).name("Criminal Records").isActive(true).build())
                .source(WatchlistSource.builder().code("UN_SC").name("UN Security Council").type(SourceType.OFFICIAL).credibilityWeight(1.0).active(true).build())
                .severity(SeverityLevel.HIGH)
                .isActive(true)
                .build();

        CandidateScreeningData candidate = CandidateScreeningData.builder()
                .candidateId(99L)
                .fullName("John Doe")
                .panNumber(null)
                .aadhaarNumber(null)
                .build();

        when(watchlistEntryRepository.findAllActiveOnDate(LocalDate.now())).thenReturn(List.of(entry));
        when(riskScoringEngine.corroborationMultiplier(any())).thenReturn(1.0);
        when(riskScoringEngine.categoryBonus(entry)).thenReturn(15.0);
        when(riskScoringEngine.calculateScore(any())).thenReturn(65.0);
        when(riskScoringEngine.classify(65.0)).thenReturn(RiskLevel.HIGH);

        ScreeningResultDto result = strategy.screen(candidate);

        assertAll(
                () -> assertEquals("BASIC", result.getStrategyUsed()),
                () -> assertEquals(65.0, result.getRiskScore(), 0.0001),
                () -> assertEquals(RiskLevel.HIGH, result.getRiskLevel()),
                () -> assertEquals(ScreeningStatus.FLAGGED, result.getStatus()),
                () -> assertEquals(1, result.getTotalEntriesChecked()),
                () -> assertEquals(1, result.getMatches().size()),
                () -> assertEquals("John Doe", result.getMatches().get(0).getWatchlistPrimaryName()),
                () -> assertEquals(55.0, result.getMatches().get(0).getScoreContribution(), 0.0001)
        );
    }
}


