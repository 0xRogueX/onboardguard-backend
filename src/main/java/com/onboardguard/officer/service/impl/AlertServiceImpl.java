package com.onboardguard.officer.service.impl;


import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.officer.dto.AlertDetailDto;
import com.onboardguard.officer.entity.Alert;
import com.onboardguard.officer.entity.Case;
import com.onboardguard.officer.entity.CaseNote;
import com.onboardguard.officer.mapper.AlertMapper;
import com.onboardguard.officer.repository.AlertRepository;
import com.onboardguard.officer.repository.CaseRepository;
import com.onboardguard.officer.service.AlertService;
import com.onboardguard.screening.entity.ScreeningResult;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.shared.common.enums.AlertStatus;
import com.onboardguard.shared.common.enums.CaseStatus;
import com.onboardguard.shared.common.enums.NoteType;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import com.onboardguard.shared.common.exception.UnauthorizedAccessException;
import com.onboardguard.shared.common.events.AlertGeneratedEvent;
import com.onboardguard.shared.config.ConfigConstants;
import com.onboardguard.shared.config.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final CaseRepository caseRepository;
    private final AlertMapper alertMapper;
    private final SystemConfigService systemConfigService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALERT_VIEW')")
    public List<AlertDetailDto> getOpenAlertsQueue() {
        return alertRepository
                .findOpenAlertsForQueue(AlertStatus.OPEN)
                .stream()
                .map(alertMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ALERT_CLAIM')")
    public AlertDetailDto acknowledgeAlert(Long alertId, Long officerId){
        
        Alert alert = alertRepository.findByIdForUpdate(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        if(alert.getStatus() != AlertStatus.OPEN){
            throw new BadRequestException("This alert is already claimed by another officer or no longer open.");
        }

        alert.setStatus(AlertStatus.IN_REVIEW);  // for locking the alert when it is reviewing by an officer , so that other officer can’t be able to see that  same alert
        alert.setAcknowledgedBy(officerId);
        alert.setAcknowledgedAt(Instant.now());

        log.info("Alert ID {} claimed and locked by L1 Officer ID {}", alertId, officerId);
        return alertMapper.toDto(alertRepository.save(alert));
    }

    /**
     * GET NEXT ALERT: Fetches the most urgent OPEN alert and instantly locks it for the officer.
     */
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ALERT_CLAIM')")
    public AlertDetailDto claimNextAvailableAlert(Long officerId) {

        // 1. Fetch the oldest open alert safely with a Pessimistic DB Lock
        Alert oldestOpenAlert = alertRepository.findFirstByStatusOrderBySlaDeadlineAsc(AlertStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("The queue is completely empty. Great job!"));

        // 2. Lock and Claim the alert for this specific officer
        oldestOpenAlert.setStatus(AlertStatus.IN_REVIEW);
        oldestOpenAlert.setAcknowledgedBy(officerId);
        oldestOpenAlert.setAcknowledgedAt(Instant.now());

        log.info("L1 Officer ID {} used 'Get Next' and was assigned Alert ID {}", officerId, oldestOpenAlert.getId());

        // 3. Save and return the mapped DTO to the frontend
        return alertMapper.toDto(alertRepository.save(oldestOpenAlert));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ALERT_DISMISS')")
    public void dismissAlert(Long alertId, Long officerId, String reason) {
        Alert alert = getAlertById(alertId);

        validateAlertOwnership(alert, officerId);

        alert.setStatus(AlertStatus.CLOSED);
        alertRepository.save(alert);

        log.info("Alert ID {} CLOSED as false positive by L1 Officer ID {}. Reason: {}", alertId, officerId, reason);

        // Note: Publish an event here if you want the Screening Engine to know the alert was dismissed
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ALERT_CONVERT_TO_CASE')")
    public Long convertToCase(Long alertId, Long officerId){
       Alert alert = getAlertById(alertId);

       validateAlertOwnership(alert, officerId);

       alert.setStatus(AlertStatus.CONVERTED_TO_CASE);
       alertRepository.save(alert);

        // 2. Start the Case Clock (e.g., 5-day SLA)
        Case investigationCase = Case.builder()
                .alertId(alert.getId())
                .candidateId(alert.getCandidateId())
                .assignedOfficerId(officerId) // The L1 who converted it automatically owns the new Case
                .assignedBy(officerId)
                .assignedAt(Instant.now())
                .status(CaseStatus.IN_REVIEW) // Start IN_REVIEW because it's actively assigned to this L1
                .slaDueDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .isSlaBreached(false)
                .build();

        // 3. Auto-generate the immutable system note
        CaseNote systemNote = CaseNote.builder()
                .investigationCase(investigationCase)
                .authorId(officerId)
                .content("Case automatically generated from Alert ID " + alertId + " by L1 Officer.")
                .noteType(NoteType.SYSTEM_ACTION)
                .build();

        investigationCase.getNotes().add(systemNote);

        Case savedCase = caseRepository.save(investigationCase);
        log.info("Alert ID {} converted to Case ID {} by L1 Officer ID {}", alertId, savedCase.getId(), officerId);

        return savedCase.getId();

    }

    /**
     * CREATE ALERT: Generates an Alert record from a completed ScreeningResult with HIGH/MEDIUM risk.
     * Automatically computes SLA deadline from SystemConfig and publishes AlertGeneratedEvent to notify officers.
     * This is the missing link between Screening Engine and Officer Alert Queue!
     */
    @Override
    @Transactional
    public void createAlert(ScreeningResult screeningResult) {
        try {
            // 1. Only create alerts for MEDIUM or HIGH risk
            if (screeningResult.getRiskLevel() != RiskLevel.MEDIUM && screeningResult.getRiskLevel() != RiskLevel.HIGH) {
                log.debug("Skipping alert creation for candidateId={} — risk level is {}",
                    screeningResult.getCandidate().getId(), screeningResult.getRiskLevel());
                return;
            }

            // 2. Map RiskLevel to SeverityLevel for alert
            SeverityLevel severity = mapRiskLevelToSeverity(screeningResult.getRiskLevel());

            // 3. Fetch candidate for context
            Candidate candidate = screeningResult.getCandidate();

            // 4. Extract matched watchlist categories from screening matches
            List<String> matchedCategories = screeningResult.getMatches().stream()
                    .map(match -> match.getWatchlistEntry().getCategory().getName())
                    .distinct()
                    .collect(Collectors.toList());

            // 5. Get SLA hours from SystemConfig (default 48 hours per requirements)
            Integer slaHours = systemConfigService.getInt(
                    ConfigConstants.SLA_HOURS,
                    ConfigConstants.Defaults.SLA_HOURS);

            // 6. Create the Alert entity
            Alert alert = Alert.builder()
                    .candidateId(candidate.getId())
                    .screeningResultId(screeningResult.getId())
                    .severity(severity)
                    .status(AlertStatus.OPEN)  // Starts in OPEN state — waiting for L1 Officer
                    .matchedCategories(matchedCategories)
                    .slaDeadline(Instant.now().plus(slaHours, ChronoUnit.HOURS))
                    .isSlaBreached(false)
                    .build();

            // 7. Persist the alert
            Alert savedAlert = alertRepository.save(alert);

            // 8. Publish event for email notification
            publishAlertNotificationEvent(savedAlert, candidate);

            log.info("Alert ID {} created for candidateId={} with severity={} and SLA deadline in {} hours",
                    savedAlert.getId(), candidate.getId(), severity, slaHours);

        } catch (Exception ex) {
            log.error("Failed to create alert from screening result ID: {}", screeningResult.getId(), ex);
            // Don't rethrow — screening should not fail if alert creation fails
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Alert getAlertById(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));
    }

    private SeverityLevel mapRiskLevelToSeverity(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> SeverityLevel.HIGH;
            case MEDIUM -> SeverityLevel.MEDIUM;
            case LOW -> SeverityLevel.LOW;
        };
    }

    private void publishAlertNotificationEvent(Alert alert, Candidate candidate) {
        // Get the first admin or officer email to notify (system admin by default)
        String notificationEmail = "admin@onboardguard.com"; // Default system email

        AlertGeneratedEvent event = new AlertGeneratedEvent(
                notificationEmail,
                alert.getId(),
                candidate.getFullName(),
                alert.getSeverity().toString()
        );

        eventPublisher.publishEvent(event);
    }

    /**
     * Guarantees that only the officer who claimed the alert can close or convert it.
     */
    private void validateAlertOwnership(Alert alert, Long officerId) {
        if (alert.getStatus() != AlertStatus.IN_REVIEW) {
            throw new BadRequestException("Alert must be IN_REVIEW before it can be processed. Please claim it first.");
        }

        if (!officerId.equals(alert.getAcknowledgedBy())) {
            log.warn("Security Alert: Officer ID {} attempted to modify Alert ID {} owned by Officer ID {}",
                    officerId, alert.getId(), alert.getAcknowledgedBy());
            throw new UnauthorizedAccessException("You cannot process this alert because it is locked by another officer.");
        }
    }
}
