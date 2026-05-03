package com.onboardguard.screening.service;

import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.enums.CorroborationLevel;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.shared.config.ConfigConstants;
import com.onboardguard.shared.config.service.SystemConfigService;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts a list of MatchDetailDtos into a single risk score,
 * and classifies that score as LOW / MEDIUM / HIGH.
 * <p>
 * All thresholds, multipliers, and bonuses are read live from SystemConfig on every call
 * so that admin changes take effect immediately without a restart.
 * <p>
 * Score formula per match:
 * contribution = (basePoints × sourceCredibility × corroborationMultiplier) + categoryBonus
 * <p>
 * Final score = sum of all contributions, capped at 100.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskScoringEngine {


    private final SystemConfigService systemConfigService;


    public double calculateScore(List<MatchDetailDto> matches) {
        if (matches == null || matches.isEmpty()) return 0.0;

        double total = matches.stream()
                .mapToDouble(MatchDetailDto::getScoreContribution)
                .sum();

        return Math.min(total, 100.0);
    }


    public RiskLevel classify(double score) {
        double highThreshold = getHighThreshold();
        double mediumThreshold = getMediumThreshold();

        if (score >= highThreshold) return RiskLevel.HIGH;
        if (score >= mediumThreshold) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }


    public double corroborationMultiplier(CorroborationLevel level) {
        return switch (level) {
            case NAME_ONLY ->
                    systemConfigService.getDouble(ConfigConstants.SCREENING_MULT_NAME_ONLY, ConfigConstants.Defaults.MULT_NAME_ONLY);
            case NAME_AND_ONE_ID ->
                    systemConfigService.getDouble(ConfigConstants.SCREENING_MULT_NAME_ONE_ID, ConfigConstants.Defaults.MULT_NAME_ONE_ID);
            case NAME_AND_TWO_IDS ->
                    systemConfigService.getDouble(ConfigConstants.SCREENING_MULT_NAME_TWO_IDS, ConfigConstants.Defaults.MULT_NAME_TWO_IDS);
            case NAME_AND_ORG ->
                    systemConfigService.getDouble(ConfigConstants.SCREENING_MULT_NAME_ORG, ConfigConstants.Defaults.MULT_NAME_ORG);
            case NAME_ORG_AND_DESIGNATION ->
                    systemConfigService.getDouble(ConfigConstants.SCREENING_MULT_NAME_ORG_DESIGNATION, ConfigConstants.Defaults.MULT_NAME_ORG_DESIGNATION);
        };
    }


    public double categoryBonus(WatchlistEntry entry) {
        double bonus = 0.0;

        try {
            CategoryCode code = entry.getCategory().getCode();
            if (code == CategoryCode.CRIMINAL) {
                bonus += systemConfigService.getDouble(ConfigConstants.SCREENING_BONUS_CRIMINAL, ConfigConstants.Defaults.BONUS_CRIMINAL);
            } else if (code == CategoryCode.PEP) {
                bonus += systemConfigService.getDouble(ConfigConstants.SCREENING_BONUS_PEP, ConfigConstants.Defaults.BONUS_PEP);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unknown category code '{}' on WatchlistEntry ID {} — no category bonus applied", entry.getCategory().getCode(), entry.getId());
        }

        if (entry.getSeverity() == SeverityLevel.HIGH) {
            bonus += systemConfigService.getDouble(ConfigConstants.SCREENING_BONUS_SEVERITY_HIGH, ConfigConstants.Defaults.BONUS_SEVERITY_HIGH);
        }

        return bonus;
    }


    public double getMediumThreshold() {
        return systemConfigService.getDouble(ConfigConstants.SCREENING_THRESHOLD_MEDIUM, ConfigConstants.Defaults.MEDIUM_THRESHOLD);
    }

    public double getHighThreshold() {
        return systemConfigService.getDouble(ConfigConstants.SCREENING_THRESHOLD_HIGH, ConfigConstants.Defaults.HIGH_THRESHOLD);
    }

    public double getFuzzyThreshold() {
        return systemConfigService.getDouble(ConfigConstants.SCREENING_THRESHOLD_FUZZY, ConfigConstants.Defaults.FUZZY_THRESHOLD);
    }
}