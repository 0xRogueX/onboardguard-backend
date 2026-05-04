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
import com.onboardguard.watchlist.entity.WatchlistAlias;
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
class AdvancedScreeningStrategyTest {

    @Mock
    private WatchlistEntryRepository watchlistEntryRepository;

    @Mock
    private RiskScoringEngine riskScoringEngine;

    private AdvancedScreeningStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AdvancedScreeningStrategy(watchlistEntryRepository, riskScoringEngine, new NameMatchingUtil());
    }

    @Test
    void screenShouldMatchKnownAliasAndReturnFlaggedResult() {
        WatchlistEntry entry = WatchlistEntry.builder()
                .id(21L)
                .primaryName("Rohit Sharma")
                .primaryNameNormalized("rohit sharma")
                .category(WatchlistCategory.builder().code(CategoryCode.FRAUD).name("Known Fraudsters").isActive(true).build())
                .source(WatchlistSource.builder().code("NEWS_MEDIA").name("Global News Media").type(SourceType.UNVERIFIED).credibilityWeight(0.4).active(true).build())
                .severity(SeverityLevel.MEDIUM)
                .isActive(true)
                .aliases(List.of(
                        WatchlistAlias.builder().aliasName("R. Sharma").aliasNameNormalized("r sharma").build()
                ))
                .build();

        CandidateScreeningData candidate = CandidateScreeningData.builder()
                .candidateId(77L)
                .fullName("R Sharma")
                .organizationName("Acme Pvt Ltd")
                .build();

        when(watchlistEntryRepository.findAllActiveOnDateWithAliases(LocalDate.now())).thenReturn(List.of(entry));
        when(riskScoringEngine.getFuzzyThreshold()).thenReturn(0.90);
        when(riskScoringEngine.corroborationMultiplier(any())).thenReturn(1.0);
        when(riskScoringEngine.categoryBonus(entry)).thenReturn(0.0);
        when(riskScoringEngine.calculateScore(any())).thenReturn(40.0);
        when(riskScoringEngine.classify(40.0)).thenReturn(RiskLevel.MEDIUM);

        ScreeningResultDto result = strategy.screen(candidate);

        assertAll(
                () -> assertEquals("ADVANCED", result.getStrategyUsed()),
                () -> assertEquals(RiskLevel.MEDIUM, result.getRiskLevel()),
                () -> assertEquals(ScreeningStatus.FLAGGED, result.getStatus()),
                () -> assertEquals(1, result.getTotalEntriesChecked()),
                () -> assertFalse(result.getMatches().isEmpty()),
                () -> assertTrue(result.getMatches().get(0).getMatchType().name().startsWith("NAME"))
        );
    }
}


