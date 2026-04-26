package com.onboardguard.screening.strategy;

import com.onboardguard.screening.dto.CandidateScreeningData;
import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.enums.*;
import com.onboardguard.screening.service.RiskScoringEngine;
import com.onboardguard.screening.util.NameMatchingUtil;
import com.onboardguard.screening.util.NameMatchingUtil.NameMatchResult;
import com.onboardguard.watchlist.entity.WatchlistAlias;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.repository.WatchlistEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Advanced screening with four layers:
 *   Layer 1 — Initials expansion   ("R.S. Sharma" matches "Rohit S. Sharma")
 *   Layer 2 — Fuzzy matching       ("Rohit Shrma" matches "Rohit Sharma")
 *   Layer 3 — Alias lookup         ("Ravi Kapoor" matches because it is an alias)
 *   Layer 4 — Multi-field corroboration (score multipliers based on ID matches)
 *
 * The fuzzy threshold is loaded from config so admin can change it at runtime.
 */
@Slf4j
@Service("advancedScreeningStrategy")
@RequiredArgsConstructor
public class AdvancedScreeningStrategy implements ScreeningStrategy {

    private final WatchlistEntryRepository watchlistEntryRepository;
    private final RiskScoringEngine riskScoringEngine;
    private final NameMatchingUtil nameMatchingUtil;

    // fuzzy threshold is read dynamically from riskScoringEngine

    @Override
    public String strategyName() {
        return "ADVANCED";
    }

    @Override
    public ScreeningResultDto screen(CandidateScreeningData candidate) {
        Instant startedAt = Instant.now();
        log.info("[ADVANCED] Starting screening for candidateId={}", candidate.getCandidateId());

        List<WatchlistEntry> activeEntries = watchlistEntryRepository
                .findAllActiveOnDateWithAliases(LocalDate.now()); // JOIN FETCH aliases
        log.info("[ADVANCED] Checking against {} active watchlist entries", activeEntries.size());

        List<MatchDetailDto> allMatches = new ArrayList<>();

        for (WatchlistEntry entry : activeEntries) {
            // One entry can produce multiple match records (e.g. name match + PAN match)
            // but we deduplicate so we don't double-count the same field twice
            List<MatchDetailDto> entryMatches = checkEntryAdvanced(candidate, entry);
            allMatches.addAll(entryMatches);
        }

        double rawScore   = riskScoringEngine.calculateScore(allMatches);
        double finalScore = Math.min(rawScore, 100.0);
        RiskLevel riskLevel = riskScoringEngine.classify(finalScore);
        ScreeningStatus status = riskLevel == RiskLevel.LOW ? ScreeningStatus.CLEAR : ScreeningStatus.FLAGGED;

        log.info("[ADVANCED] Completed candidateId={} score={} level={} matches={}",
                candidate.getCandidateId(), finalScore, riskLevel, allMatches.size());

        return ScreeningResultDto.builder()
                .candidateId(candidate.getCandidateId())
                .strategyUsed(strategyName())
                .riskScore(finalScore)
                .riskLevel(riskLevel)
                .status(status)
                .matches(allMatches)
                .totalEntriesChecked(activeEntries.size())
                .screeningStartedAt(startedAt)
                .screeningCompletedAt(Instant.now())
                .build();
    }

    // ── Per-entry advanced checks ─────────────────────────────────────────────

    private List<MatchDetailDto> checkEntryAdvanced(CandidateScreeningData c, WatchlistEntry entry) {
        List<MatchDetailDto> matches = new ArrayList<>();

        // ── LAYER 1 + 2: Name match against primary name ──────────────────────
        Optional<MatchDetailDto> nameMatch = checkNameMatch(
                c, entry, c.getFullName(), entry.getPrimaryName(), false);
        nameMatch.ifPresent(matches::add);

        // ── LAYER 3: Alias lookup (only if name didn't already match) ─────────
        // If name already matched the primary name, aliases won't add score —
        // same entry, same candidate, just a different comparison path.
        if (nameMatch.isEmpty()) {
            for (WatchlistAlias alias : entry.getAliases()) {
                Optional<MatchDetailDto> aliasMatch = checkNameMatch(
                        c, entry, c.getFullName(), alias.getAliasName(), true);
                if (aliasMatch.isPresent()) {
                    matches.add(aliasMatch.get());
                    break; // One alias match per entry is enough
                }
            }
        }

        // ── LAYER 4 corroborating fields ──────────────────────────────────────
        // These are only added if there is also a name (or alias) match,
        // because standalone ID matches without a name match are low value.
        boolean hasNameMatch = !matches.isEmpty();

        if (hasNameMatch) {
            // PAN
            if (isNotBlank(c.getPanNumber()) && isNotBlank(entry.getPanNumber())
                    && c.getPanNumber().equalsIgnoreCase(entry.getPanNumber())) {
                matches.add(buildCorroboratingMatch(c, entry,
                        MatchType.PAN_EXACT,
                        c.getPanNumber(), entry.getPanNumber(), null, 35.0));
            }

            // Aadhaar
            if (isNotBlank(c.getAadhaarNumber()) && isNotBlank(entry.getAadhaarNumber())
                    && c.getAadhaarNumber().equals(entry.getAadhaarNumber())) {
                matches.add(buildCorroboratingMatch(c, entry,
                        MatchType.AADHAAR_EXACT,
                        c.getAadhaarNumber(), entry.getAadhaarNumber(), null, 30.0));
            }

            // Organization — fuzzy allowed here too
            if (isNotBlank(c.getOrganizationNameNormalized())
                    && isNotBlank(entry.getOrganizationName())) {
                NameMatchResult orgResult = nameMatchingUtil.advancedNameMatch(
                        c.getOrganizationName(), entry.getOrganizationName(), riskScoringEngine.getFuzzyThreshold());
                if (orgResult.matched()) {
                    MatchType orgType = orgResult.exact() ? MatchType.ORG_EXACT : MatchType.ORG_FUZZY;
                    matches.add(buildCorroboratingMatch(c, entry,
                            orgType,
                            c.getOrganizationName(), entry.getOrganizationName(),
                            orgResult.similarity(), 20.0));
                }
            }

            // Designation — exact only (too short for reliable fuzzy)
            if (isNotBlank(c.getDesignation()) && isNotBlank(entry.getDesignation())
                    && c.getDesignation().equalsIgnoreCase(entry.getDesignation())) {
                matches.add(buildCorroboratingMatch(c, entry,
                        MatchType.DESIGNATION_EXACT,
                        c.getDesignation(), entry.getDesignation(), null, 15.0));
            }
        }

        // Re-compute corroboration level now that we know all matches for this entry
        if (!matches.isEmpty()) {
            CorroborationLevel corr = resolveCorroborationFromMatches(matches);
            // Update the corroboration level and recompute contributions on all matches for this entry
            matches = recomputeWithCorroboration(matches, corr, entry);
        }

        return matches;
    }

    // ── Name match helper (primary name or alias) ────────────────────────────

    private Optional<MatchDetailDto> checkNameMatch(
            CandidateScreeningData c,
            WatchlistEntry entry,
            String candidateName,
            String watchlistName,
            boolean isAlias) {

        NameMatchResult result = nameMatchingUtil.advancedNameMatch(
                candidateName, watchlistName, riskScoringEngine.getFuzzyThreshold());

        if (!result.matched()) return Optional.empty();

        MatchType type;
        double basePoints;

        if (isAlias) {
            type       = result.exact() ? MatchType.NAME_ALIAS_EXACT : MatchType.NAME_ALIAS_FUZZY;
            basePoints = 20.0;
        } else {
            type       = result.exact() ? MatchType.NAME_EXACT : MatchType.NAME_FUZZY;
            basePoints = result.exact() ? 40.0 : 25.0;
        }

        // Corroboration determined later when we know all matching fields for this entry
        // Use NAME_ONLY as placeholder — will be recomputed below
        double credibility    = entry.getSource().getCredibilityWeight();
        double corrMultiplier = riskScoringEngine.corroborationMultiplier(CorroborationLevel.NAME_ONLY);
        double categoryBonus  = riskScoringEngine.categoryBonus(entry);
        double contribution   = (basePoints * credibility * corrMultiplier) + categoryBonus;

        return Optional.of(MatchDetailDto.builder()
                .watchlistEntryId(entry.getId())
                .watchlistPrimaryName(entry.getPrimaryName())
                .watchlistCategory(entry.getCategory().getCode().name())
                .watchlistSeverity(entry.getSeverity().name())
                .watchlistSourceName(entry.getSource().getName())
                .watchlistSourceCredibility(credibility)
                .matchType(type)
                .candidateFieldValue(candidateName)
                .watchlistFieldValue(watchlistName)
                .similarityScore(result.exact() ? null : result.similarity())
                .basePoints(basePoints)
                .sourceCredibilityWeight(credibility)
                .corroborationMultiplier(corrMultiplier)
                .categoryBonus(categoryBonus)
                .scoreContribution(contribution)
                .corroborationLevel(CorroborationLevel.NAME_ONLY)
                .build());
    }

    /**
     * After collecting all matches for one entry, determine the corroboration level
     * and recompute contributions with the correct multiplier.
     */
    private CorroborationLevel resolveCorroborationFromMatches(List<MatchDetailDto> matches) {
        boolean hasPan     = matches.stream().anyMatch(m -> m.getMatchType() == MatchType.PAN_EXACT);
        boolean hasAadhaar = matches.stream().anyMatch(m -> m.getMatchType() == MatchType.AADHAAR_EXACT);
        boolean hasOrg     = matches.stream().anyMatch(m ->
                m.getMatchType() == MatchType.ORG_EXACT || m.getMatchType() == MatchType.ORG_FUZZY);
        boolean hasDesig   = matches.stream().anyMatch(m -> m.getMatchType() == MatchType.DESIGNATION_EXACT);

        if (hasPan && hasAadhaar)   return CorroborationLevel.NAME_AND_TWO_IDS;
        if (hasPan || hasAadhaar)   return CorroborationLevel.NAME_AND_ONE_ID;
        if (hasOrg && hasDesig)     return CorroborationLevel.NAME_ORG_AND_DESIGNATION;
        if (hasOrg)                 return CorroborationLevel.NAME_AND_ORG;
        return                             CorroborationLevel.NAME_ONLY;
    }

    /**
     * Rebuild match DTOs with the correct corroboration level and recalculated scores.
     * Required because corroboration can only be known after checking all fields.
     */
    private List<MatchDetailDto> recomputeWithCorroboration(
            List<MatchDetailDto> original,
            CorroborationLevel corr,
            WatchlistEntry entry) {

        double corrMultiplier = riskScoringEngine.corroborationMultiplier(corr);
        double categoryBonus  = riskScoringEngine.categoryBonus(entry);
        List<MatchDetailDto> updated = new ArrayList<>();

        for (MatchDetailDto m : original) {
            boolean isNameRow = m.getMatchType().name().startsWith("NAME");
            double bonusForThisMatch = isNameRow ? categoryBonus : 0.0;
            double contribution = (m.getBasePoints() * m.getSourceCredibilityWeight() * corrMultiplier)
                    + bonusForThisMatch;

            updated.add(MatchDetailDto.builder()
                    .watchlistEntryId(m.getWatchlistEntryId())
                    .watchlistPrimaryName(m.getWatchlistPrimaryName())
                    .watchlistCategory(m.getWatchlistCategory())
                    .watchlistSeverity(m.getWatchlistSeverity())
                    .watchlistSourceName(m.getWatchlistSourceName())
                    .watchlistSourceCredibility(m.getWatchlistSourceCredibility())
                    .matchType(m.getMatchType())
                    .candidateFieldValue(m.getCandidateFieldValue())
                    .watchlistFieldValue(m.getWatchlistFieldValue())
                    .similarityScore(m.getSimilarityScore())
                    .basePoints(m.getBasePoints())
                    .sourceCredibilityWeight(m.getSourceCredibilityWeight())
                    .corroborationMultiplier(corrMultiplier)
                    .categoryBonus(bonusForThisMatch)
                    .scoreContribution(contribution)
                    .corroborationLevel(corr)
                    .build());
        }
        return updated;
    }

    private MatchDetailDto buildCorroboratingMatch(
            CandidateScreeningData c,
            WatchlistEntry entry,
            MatchType type,
            String candidateValue,
            String watchlistValue,
            Double similarity,
            double basePoints) {

        double credibility    = entry.getSource().getCredibilityWeight();
        // Corroboration multiplier will be recomputed by recomputeWithCorroboration()
        // Use 1.0 as placeholder here
        double categoryBonus  = 0.0; // category bonus only added once (on name match)
        double contribution   = basePoints * credibility * 1.0;

        return MatchDetailDto.builder()
                .watchlistEntryId(entry.getId())
                .watchlistPrimaryName(entry.getPrimaryName())
                .watchlistCategory(entry.getCategory().getCode().name())
                .watchlistSeverity(entry.getSeverity().name())
                .watchlistSourceName(entry.getSource().getName())
                .watchlistSourceCredibility(credibility)
                .matchType(type)
                .candidateFieldValue(candidateValue)
                .watchlistFieldValue(watchlistValue)
                .similarityScore(similarity)
                .basePoints(basePoints)
                .sourceCredibilityWeight(credibility)
                .corroborationMultiplier(1.0)
                .categoryBonus(categoryBonus)
                .scoreContribution(contribution)
                .corroborationLevel(CorroborationLevel.NAME_ONLY)
                .build();
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}