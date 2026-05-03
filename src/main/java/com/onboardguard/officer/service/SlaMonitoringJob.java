package com.onboardguard.officer.service;

import com.onboardguard.officer.entity.Alert;
import com.onboardguard.officer.entity.Case;
import com.onboardguard.officer.repository.AlertRepository;
import com.onboardguard.officer.repository.CaseRepository;
import com.onboardguard.shared.common.enums.AlertStatus;
import com.onboardguard.shared.common.enums.CaseStatus;
import com.onboardguard.shared.common.events.SlaBreachNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaMonitoringJob {

    private final AlertRepository alertRepository;
    private final CaseRepository caseRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs strictly every 3 minutes.
     * Cron expression breakdown: "0 0/3 * * * *" -> At 0 seconds, every 3 minutes, every hour, every day.
     */
    @Scheduled(cron = "0 0/3 * * * *")
    @Transactional
    public void monitorSlaBreaches() {
        log.info("[SLA MONITOR] Starting 3-minute SLA check...");
        Instant now = Instant.now();

        try {
            // 1. Process Alerts (Only OPEN or IN_REVIEW can breach)
            List<AlertStatus> activeAlertStatuses = List.of(
                    AlertStatus.OPEN,
                    AlertStatus.IN_REVIEW
            );
            int breachedAlerts = alertRepository.markBreachedAlerts(activeAlertStatuses, now);

            // 2. Process Cases (Anything except RESOLVED can breach)
            int breachedCases = caseRepository.markBreachedCases(CaseStatus.RESOLVED, now);

            // 3. Log the final tally
            if (breachedAlerts > 0 || breachedCases > 0) {
                log.warn("[SLA MONITOR] Breach Detected! System flagged {} Alerts and {} Cases.",
                        breachedAlerts, breachedCases);

                eventPublisher.publishEvent(new SlaBreachNotificationEvent(breachedAlerts, breachedCases , now));
                // This could send a Slack or Email alert to the Compliance Manager!
            } else {
                log.info("[SLA MONITOR] Check complete. No new breaches detected. The team is on track!");
            }

        } catch (Exception e) {
            log.error("[SLA MONITOR] Failed to execute SLA updates due to a database error.", e);
        }
    }
}