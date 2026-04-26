package com.onboardguard.screening.dto;

import com.onboardguard.screening.enums.CorroborationLevel;
import com.onboardguard.screening.enums.MatchType;
import lombok.Builder;

/**
 * Represents one individual match found during screening.
 * Collected by strategies and returned inside ScreeningResultDto.
 */
@Builder
public record MatchDetailDto (
        Long   watchlistEntryId,
        String watchlistPrimaryName,
        String watchlistCategory,
        String watchlistSeverity,
        String watchlistSourceName,
        Double watchlistSourceCredibility,

        // What type of match was found
        MatchType matchType,

        // The actual values compared
        String candidateFieldValue,
        String watchlistFieldValue,

        // Similarity score — null for exact matches
        Double similarityScore,

        // Score breakdown
        Double basePoints,
        Double sourceCredibilityWeight,
        Double corroborationMultiplier,
        Double categoryBonus,
        Double scoreContribution,

        CorroborationLevel corroborationLevel
) { }