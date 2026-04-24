package com.onboardguard.admin.dto;

import com.onboardguard.shared.common.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewApprovalRequestDto {

    @NotNull(message = "Review status is required")
    private RequestStatus status; // APPROVED or REJECTED

    // Enforced in service layer if status == REJECTED
    private String rejectionReason;

    private boolean isBypass = false;
}