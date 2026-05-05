package com.onboardguard.screening.service;


import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.repository.CandidateRepository;
import com.onboardguard.officer.service.AlertService;
import com.onboardguard.screening.dto.CandidateScreeningData;
import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.entity.ScreeningMatch;
import com.onboardguard.screening.entity.ScreeningResult;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import com.onboardguard.screening.mapper.ScreeningMapper;
import com.onboardguard.screening.repository.ScreeningResultRepository;
import com.onboardguard.screening.strategy.ScreeningStrategy;
import com.onboardguard.shared.config.ConfigConstants;
import com.onboardguard.shared.config.service.SystemConfigService;
import com.onboardguard.watchlist.repository.WatchlistEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.shared.common.events.CaseResolvedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningOrchestrationService {


    private final Map<String, ScreeningStrategy> strategyMap;

    private final CandidateRepository candidateRepository;
    private final ScreeningResultRepository screeningResultRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;
    private final SystemConfigService systemConfigService;
    private final RiskScoringEngine riskScoringEngine;
    private final ScreeningMapper screeningMapper;
    private final AlertService alertService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCREENING_RESCREEN','SCREENING_CANDIDATE')")
    public ScreeningResultDto runScreening(Long candidateId) {
        log.info("Screening triggered for candidateId={}", candidateId);

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found: " + candidateId));

        if (screeningResultRepository.existsByCandidateIdAndStatus(candidateId, ScreeningStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Screening already in progress for candidate: " + candidateId);
        }

        ScreeningStrategy strategy = resolveActiveStrategy();
        log.info("Using strategy={} for candidateId={}", strategy.strategyName(), candidateId);

        CandidateScreeningData candidateData = screeningMapper.toCandidateScreeningData(candidate);

        // Create PENDING result row immediately (candidate sees "In Progress")
        ScreeningResult pendingResult = createPendingResult(candidate, strategy.strategyName());

        pendingResult.setStatus(ScreeningStatus.IN_PROGRESS);
        pendingResult.setScreeningStartedAt(Instant.now());
        screeningResultRepository.save(pendingResult);

        candidate.setScreeningStatus(ScreeningStatus.IN_PROGRESS);
        candidateRepository.save(candidate);

        try {
            ScreeningResultDto resultDto = strategy.screen(candidateData);

            screeningMapper.updateScreeningResultFromDto(resultDto, pendingResult);

            persistMatches(resultDto, pendingResult);

            ScreeningResult savedResult = screeningResultRepository.save(pendingResult);

            // Update candidate status using mapper helper (riskLevelToStatus)
            ScreeningStatus newStatus = ScreeningStatus.valueOf(screeningMapper.riskLevelToStatus(resultDto.getRiskLevel()).name());
            candidate.setScreeningStatus(newStatus);
            candidateRepository.save(candidate);

            // Trigger alert if MEDIUM or HIGH - NOW ACTIVE
            if (resultDto.getRiskLevel() == RiskLevel.MEDIUM
                    || resultDto.getRiskLevel() == RiskLevel.HIGH) {
                alertService.createAlert(savedResult);
            } else {
                // AUTO-APPROVE if LOW risk
                candidate.setOnboardingStatus(OnboardingStatus.APPROVED);
                candidateRepository.save(candidate);

                // Send clearance email
                eventPublisher.publishEvent(new CaseResolvedEvent(
                        candidate.getUser().getEmail(),
                        candidate.getFullName(),
                        true,
                        "Onboarding documents verified and compliance screening cleared. You are authorized to begin your onboarding journey!"
                ));
                log.info("Candidate ID {} AUTO-APPROVED — low risk screening.", candidateId);
            }

            log.info("Screening complete candidateId={} score={} level={}",
                    candidateId, resultDto.getRiskScore(), resultDto.getRiskLevel());

            // Return summary DTO (no match list — caller fetches matches separately)
            return screeningMapper.toScreeningResultDtoSummary(savedResult);

        } catch (Exception ex) {
            pendingResult.setStatus(ScreeningStatus.PENDING);
            screeningResultRepository.save(pendingResult);
            candidate.setScreeningStatus(ScreeningStatus.PENDING);
            candidateRepository.save(candidate);
            log.error("Screening failed for candidateId={}", candidateId, ex);
            throw ex;
        }
    }

    // Dynamic DI - Read active strategy from SystemConfig
    private ScreeningStrategy resolveActiveStrategy() {
        String configured = systemConfigService.getString(
                ConfigConstants.ACTIVE_SCREENING_STRATEGY,
                ConfigConstants.Defaults.ACTIVE_STRATEGY);

        String beanName = configured.toLowerCase() + "ScreeningStrategy";
        ScreeningStrategy strategy = strategyMap.get(beanName);

        if (strategy == null) {
            log.warn("Unknown strategy '{}' — falling back to BASIC", configured);
            strategy = strategyMap.get("basicScreeningStrategy");
        }
        return strategy;
    }

    // Persistence helpers
    private ScreeningResult createPendingResult(Candidate candidate, String strategyName) {
        return screeningResultRepository.save(
                ScreeningResult.builder()
                        .candidate(candidate)
                        .strategyUsed(strategyName)
                        .riskScore(0.0)
                        .riskLevel(RiskLevel.LOW)
                        .status(ScreeningStatus.PENDING)
                        // Threshold snapshots captured NOW - immutable from this point
                        .mediumThresholdSnapshot(riskScoringEngine.getMediumThreshold())
                        .highThresholdSnapshot(riskScoringEngine.getHighThreshold())
                        .fuzzyThresholdSnapshot(riskScoringEngine.getFuzzyThreshold())
                        .build()
        );
    }

    /**
     * Converts all MatchDetailDtos -> ScreeningMatch entities via mapper,
     * then wires the watchlistEntry proxy and attaches to the result.
     */
    private void persistMatches(ScreeningResultDto resultDto, ScreeningResult result) {
        if (resultDto.getMatches() == null || resultDto.getMatches().isEmpty()) return;

        for (MatchDetailDto dto : resultDto.getMatches()) {
            // Mapper handles all field + snapshot mapping
            ScreeningMatch match = screeningMapper.toScreeningMatchEntity(dto);

            // watchlistEntry is ignored by mapper - set manually via proxy (no extra SELECT)
            match.setWatchlistEntry(
                    watchlistEntryRepository.getReferenceById(dto.getWatchlistEntryId()));

            // addMatch() wires screeningResult + adds to list (bidirectional sync)
            result.addMatch(match);
        }
    }
}