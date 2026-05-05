package com.onboardguard.officer.controller;

import com.onboardguard.officer.dto.AlertDetailDto;
import com.onboardguard.officer.service.AlertService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/officer/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDetailDto>>> getOpenAlertsQueue() {

        List<AlertDetailDto> alerts = alertService.getOpenAlertsQueue();

        return ResponseEntity.ok(
                ApiResponse.success("Open alerts retrieved successfully.", alerts)
        );
    }

    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<AlertDetailDto>> acknowledgeAlert(@PathVariable Long alertId) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        AlertDetailDto claimedAlert = alertService.acknowledgeAlert(alertId, currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success("Alert successfully claimed and locked.", claimedAlert));
    }


     // This enforces a strict FIFO (First-In, First-Out) queue and prevents cherry-picking.
    @PostMapping("/assign-next")
    public ResponseEntity<ApiResponse<AlertDetailDto>> claimNextAvailableAlert() {

        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        // If the queue is empty, the service throws ResourceNotFoundException,
        // which your GlobalExceptionHandler will elegantly catch and return to the UI!
        AlertDetailDto nextAlert = alertService.claimNextAvailableAlert(currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success("Alert successfully assigned from the queue.", nextAlert));
    }

    @PostMapping("/{alertId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissAlert(
            @PathVariable Long alertId,
            @RequestParam String reason) {

        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        alertService.dismissAlert(alertId, currentOfficerId, reason);

        return ResponseEntity.ok(ApiResponse.success("Alert dismissed successfully.", null));
    }

    @PostMapping("/{alertId}/convert")
    public ResponseEntity<ApiResponse<Long>> convertToCase(@PathVariable Long alertId) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        Long newCaseId = alertService.convertToCase(alertId, currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success("Alert converted to Case successfully.", newCaseId));
    }

    @GetMapping("/breached")
    public ResponseEntity<ApiResponse<List<AlertDetailDto>>> getBreachedAlerts() {
        List<AlertDetailDto> breachedAlerts = alertService.getBreachedAlerts();
        return ResponseEntity.ok(ApiResponse.success("Breached alerts retrieved successfully.", breachedAlerts));
    }
}