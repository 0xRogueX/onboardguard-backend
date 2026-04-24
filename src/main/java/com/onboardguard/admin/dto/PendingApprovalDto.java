package com.onboardguard.admin.dto;

import com.onboardguard.shared.common.enums.ActionType;
import com.onboardguard.shared.common.enums.RequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class PendingApprovalDto {

    private Long id;

    // What is changing?
    private ActionType actionType; // e.g., CREATE, UPDATE, DELETE
    private String targetEntityType; // e.g., "SYSTEM_CONFIG", "WATCHLIST_ENTRY"
    private Long targetEntityId; // Nullable if it's a brand new creation

    // The proposed changes (Sent as a Map so the frontend can easily iterate and show "Old Value -> New Value")
    private Map<String, Object> payload;

    // Maker Info
    private Long requestedById;
    private String requestedByName; // Human readable name for the UI
    private Instant requestedAt;

    // Checker Info
    private Long reviewedById;
    private String reviewedByName;
    private Instant reviewedAt;

    // Status & Overrides
    private RequestStatus status;
    private String rejectionReason;
    private Boolean isBypass; // Flags if Super Admin used emergency override
}