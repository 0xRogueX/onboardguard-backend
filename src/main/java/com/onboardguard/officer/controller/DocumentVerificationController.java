package com.onboardguard.officer.controller;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.officer.dto.RejectDocumentRequestDto;
import com.onboardguard.officer.service.DocumentVerificationService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/officer/documents")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('ROLE_OFFICER_L1', 'ROLE_SUPER_ADMIN')") // L1 is typically the Maker for KYC docs
public class DocumentVerificationController {

    private final DocumentVerificationService documentVerificationService;
    private final SecurityUtils securityUtils;

    /**
     * GET: Pulls all documents for a specific candidate.
     * The service will attach fresh, 15-minute presigned S3 URLs to each document.
     */
    @GetMapping("/candidates/{candidateId}")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getCandidateDocuments(@PathVariable Long candidateId) {

        List<DocumentResponseDto> documents = documentVerificationService.getCandidateDocumentsForReview(candidateId);

        return ResponseEntity.ok(ApiResponse.success(
                "Candidate documents retrieved successfully for review.",
                documents
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

}