package com.onboardguard.screening.service;

import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.enums.CorroborationLevel;
import com.onboardguard.screening.enums.MatchType;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.shared.config.ConfigConstants;
import com.onboardguard.shared.config.service.SystemConfigService;
import com.onboardguard.watchlist.entity.WatchlistCategory;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.entity.WatchlistSource;
import com.onboardguard.shared.common.enums.SourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskScoringEngineTest {

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private RiskScoringEngine riskScoringEngine;

    @Test
    void calculateScoreShouldReturnZeroForEmptyInput() {
        assertAll(
                () -> assertEquals(0.0, riskScoringEngine.calculateScore(null), 0.0001),
                () -> assertEquals(0.0, riskScoringEngine.calculateScore(List.of()), 0.0001)
        );
    }

    @Test
    void calculateScoreShouldCapTotalAtHundred() {
        List<MatchDetailDto> matches = List.of(
                matchContribution(60.0),
                matchContribution(55.0)
        );

        assertEquals(100.0, riskScoringEngine.calculateScore(matches), 0.0001);
    }

    @Test
    void classifyShouldUseConfiguredThresholds() {
        when(systemConfigService.getDouble(eq(ConfigConstants.SCREENING_THRESHOLD_MEDIUM), eq(ConfigConstants.Defaults.MEDIUM_THRESHOLD)))
                .thenReturn(31.0);
        when(systemConfigService.getDouble(eq(ConfigConstants.SCREENING_THRESHOLD_HIGH), eq(ConfigConstants.Defaults.HIGH_THRESHOLD)))
                .thenReturn(61.0);

        assertAll(
                () -> assertEquals(RiskLevel.LOW, riskScoringEngine.classify(10.0)),
                () -> assertEquals(RiskLevel.MEDIUM, riskScoringEngine.classify(31.0)),
                () -> assertEquals(RiskLevel.HIGH, riskScoringEngine.classify(75.0))
        );
    }

    @Test
    void categoryBonusShouldAddCriminalAndHighSeverityBonuses() {
        when(systemConfigService.getDouble(eq(ConfigConstants.SCREENING_BONUS_CRIMINAL), eq(ConfigConstants.Defaults.BONUS_CRIMINAL)))
                .thenReturn(15.0);
        when(systemConfigService.getDouble(eq(ConfigConstants.SCREENING_BONUS_SEVERITY_HIGH), eq(ConfigConstants.Defaults.BONUS_SEVERITY_HIGH)))
                .thenReturn(10.0);

        WatchlistEntry entry = WatchlistEntry.builder()
                .id(1L)
                .category(WatchlistCategory.builder().code(CategoryCode.CRIMINAL).name("Criminal Records").isActive(true).build())
                .source(WatchlistSource.builder().code("UN_SC").name("UN Security Council").type(SourceType.OFFICIAL).credibilityWeight(1.0).active(true).build())
                .severity(SeverityLevel.HIGH)
                .build();

        assertEquals(25.0, riskScoringEngine.categoryBonus(entry), 0.0001);
    }

    @Test
    void corroborationMultiplierShouldReturnConfiguredValueForNameOnly() {
        when(systemConfigService.getDouble(eq(ConfigConstants.SCREENING_MULT_NAME_ONLY), eq(ConfigConstants.Defaults.MULT_NAME_ONLY)))
                .thenReturn(0.55);

        assertEquals(0.55, riskScoringEngine.corroborationMultiplier(CorroborationLevel.NAME_ONLY), 0.0001);
    }

    private MatchDetailDto matchContribution(double contribution) {
        return MatchDetailDto.builder()
                .scoreContribution(contribution)
                .build();
    }
}

