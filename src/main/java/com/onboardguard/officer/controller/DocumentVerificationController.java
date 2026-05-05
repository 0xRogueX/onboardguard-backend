package com.onboardguard.officer.controller;

import com.onboardguard.officer.dto.CandidateQueueItemDto;
import com.onboardguard.officer.dto.CandidateVerificationDashboardDto;
import com.onboardguard.officer.dto.RejectDocumentRequestDto;
import com.onboardguard.officer.service.DocumentVerificationService;
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
@RequestMapping("/api/v1/officer/documents")
@RequiredArgsConstructor
public class DocumentVerificationController {

    private final DocumentVerificationService documentVerificationService;
    private final SecurityUtils securityUtils;

    @PostMapping("/candidates/assign-next")
    public ResponseEntity<ApiResponse<CandidateVerificationDashboardDto>> claimNextAvailableCandidate() {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        CandidateVerificationDashboardDto dashboardData = documentVerificationService.claimNextAvailableCandidate(currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Candidate successfully assigned from the queue.",
                dashboardData
        ));
    }

    @PostMapping("/candidates/{candidateId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimCandidate(@PathVariable Long candidateId) {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();

        documentVerificationService.claimCandidateForVerification(candidateId, currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Candidate claimed successfully. Ready for review.",
                null
        ));
    }

    // ══════════════════════════════════════════════════════════════
    // 2. DASHBOARD & VERIFICATION ENDPOINTS
    // ══════════════════════════════════════════════════════════════

    /**
     * GET: Pulls the complete candidate profile (data + documents) for the Officer Dashboard.
     */
    @GetMapping("/candidates/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateVerificationDashboardDto>> getCandidateVerificationDetails(
            @PathVariable Long candidateId) {

        CandidateVerificationDashboardDto dashboardData = documentVerificationService.getCandidateVerificationDetails(candidateId);

        return ResponseEntity.ok(ApiResponse.success(
                "Candidate verification details retrieved successfully.",
                dashboardData
        ));
    }

    /**
     * POST: Approve a legally valid document.
     */
    @PostMapping("/{documentId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveDocument(@PathVariable Long documentId) {

        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        documentVerificationService.approveDocument(documentId, currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success("Document verified successfully.", null));
    }

    /**
     * POST: Reject an invalid/blurry document and trigger an email to the candidate.
     */
    @PostMapping("/{documentId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RejectDocumentRequestDto request) {

        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        documentVerificationService.rejectDocument(documentId, request.reason(), currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success("Document rejected. Candidate will be notified to re-upload.", null));
    }

    // ══════════════════════════════════════════════════════════════
    // QUEUE VIEW ENDPOINT
    // ══════════════════════════════════════════════════════════════

    /**
     * GET: Returns the queue of all candidates waiting for document verification.
     * Used to populate the Officer's "Pending Verifications" data grid.
     */
    @GetMapping("/candidates/pending")
    public ResponseEntity<ApiResponse<List<CandidateQueueItemDto>>> getPendingCandidatesQueue() {
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        List<CandidateQueueItemDto> queue = documentVerificationService.getPendingCandidatesQueue(currentOfficerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Pending candidate queue retrieved successfully.",
                queue
        ));
    }

}