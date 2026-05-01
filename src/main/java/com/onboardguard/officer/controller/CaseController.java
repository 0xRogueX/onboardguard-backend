package com.onboardguard.officer.controller;

import com.onboardguard.officer.dto.*;
import com.onboardguard.officer.service.CaseNoteService;
import com.onboardguard.officer.service.CaseService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/officer/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final CaseNoteService caseNoteService;
    private final SecurityUtils securityUtils;


    // READ OPERATIONS (Both L1 & L2)
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<CaseDetailDto>>> getAvailableCasesQueue() {
        List<CaseDetailDto> cases = caseService.getAvailableCasesForQueue();
        return ResponseEntity.ok(ApiResponse.success("Available OPEN cases retrieved successfully.", cases));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CaseDetailDto>>> getMyCases() {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        List<CaseDetailDto> cases = caseService.getMyCases(currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Your assigned cases retrieved successfully.", cases));
    }

    @GetMapping("/escalated")
    public ResponseEntity<ApiResponse<List<CaseDetailDto>>> getEscalatedCasesQueue() {
        List<CaseDetailDto> cases = caseService.getEscalatedCasesQueue();
        return ResponseEntity.ok(ApiResponse.success("Available ESCALATED cases retrieved successfully.", cases));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseDetailDto>> getCaseDetails(@PathVariable Long caseId) {
        CaseDetailDto caseDetails = caseService.getCaseDetails(caseId);
        return ResponseEntity.ok(ApiResponse.success("Case details retrieved successfully.", caseDetails));
    }


    // L1 OPERATIONS (OPEN -> IN_REVIEW)
    @PostMapping("/available/{caseId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimOpenCaseManual(@PathVariable Long caseId) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        caseService.claimOpenCaseManual(caseId, currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("OPEN case claimed successfully.", null));
    }

    @PostMapping("/available/assign-next")
    public ResponseEntity<ApiResponse<CaseDetailDto>> claimNextOpenCaseFifo() {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        CaseDetailDto assignedCase = caseService.claimNextOpenCaseFifo(currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Next OPEN case assigned successfully.", assignedCase));
    }

    @PostMapping("/{caseId}/escalate")
    public ResponseEntity<ApiResponse<Void>> escalateCase(
            @PathVariable Long caseId,
            @Valid @RequestBody EscalateCaseDto dto) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        caseService.escalateCase(caseId, dto, currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Case escalated to L2 successfully.", null));
    }


    // L2 OPERATIONS (ESCALATED -> RESOLVED)
    @PostMapping("/escalated/{caseId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimEscalatedCaseManual(@PathVariable Long caseId) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        caseService.claimEscalatedCaseManual(caseId, currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("ESCALATED case claimed successfully.", null));
    }

    @PostMapping("/escalated/assign-next")
    public ResponseEntity<ApiResponse<CaseDetailDto>> claimNextEscalatedCaseFifo() {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        CaseDetailDto assignedCase = caseService.claimNextEscalatedCaseFifo(currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Next ESCALATED case assigned successfully.", assignedCase));
    }

    @PostMapping("/{caseId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveCase(
            @PathVariable Long caseId,
            @Valid @RequestBody ResolveCaseDto dto) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        caseService.resolveCase(caseId, dto, currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Case resolved successfully.", null));
    }


    // UTILITY ENDPOINTS (Both L1 & L2)
    @PostMapping("/{caseId}/notes")
    public ResponseEntity<ApiResponse<CaseNoteDto>> addInvestigationNote(
            @PathVariable Long caseId,
            @Valid @RequestBody NoteRequest request) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        CaseNoteDto note = caseNoteService.addInvestigationNote(caseId, request.content(), currentOfficerId);
        return ResponseEntity.ok(ApiResponse.success("Note added successfully.", note));
    }
}