package com.onboardguard.admin.controller;

import com.onboardguard.admin.dto.PendingApprovalDto;
import com.onboardguard.admin.dto.ReviewApprovalRequestDto;
import com.onboardguard.admin.service.MakerCheckerService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.common.enums.RequestStatus;
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
@RequestMapping("/api/v1/admin/approvals")
@RequiredArgsConstructor
public class MakerCheckerController {

    private final MakerCheckerService makerCheckerService;
    private final SecurityUtils securityUtils;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingApprovalDto>>> getPendingInbox() {
        log.info("User {} is fetching the Maker-Checker pending inbox",
                securityUtils.getCurrentUserPrincipal().getEmail());

        List<PendingApprovalDto> pendingRequests = makerCheckerService.getPendingInbox();

        return ResponseEntity.ok(ApiResponse.success(
                "Pending approvals fetched successfully",
                pendingRequests
        ));
    }

    @PostMapping("/{requestId}/review")
    public ResponseEntity<ApiResponse<String>> processReview(
            @PathVariable Long requestId,
            @Valid @RequestBody ReviewApprovalRequestDto reviewDto) {

        String checkerEmail = securityUtils.getCurrentUserPrincipal().getEmail();

        log.info("Super Admin {} is submitting a review for Request ID: {}", checkerEmail, requestId);

        makerCheckerService.processReview(requestId, reviewDto, checkerEmail);

        String message = reviewDto.status() == RequestStatus.APPROVED
                ? "Approval request successfully APPROVED and changes applied."
                : "Approval request successfully REJECTED.";

        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}