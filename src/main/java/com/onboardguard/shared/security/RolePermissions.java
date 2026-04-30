package com.onboardguard.shared.security;

import com.onboardguard.shared.common.enums.RoleCode;
import java.util.Set;

/**
 * Central permission registry for the entire system.
 */
public final class RolePermissions {

    private RolePermissions() {}

    // ============================================================
    // CANDIDATE PERMISSIONS
    // ============================================================
    public static final String CANDIDATE_FORM_SUBMIT       = "CANDIDATE_FORM_SUBMIT";
    public static final String CANDIDATE_DOC_UPLOAD        = "CANDIDATE_DOC_UPLOAD";
    public static final String CANDIDATE_STATUS_VIEW_OWN   = "CANDIDATE_STATUS_VIEW_OWN";

    // ============================================================
    // DOCUMENT VERIFICATION
    // ============================================================
    public static final String DOC_QUEUE_VIEW      = "DOC_QUEUE_VIEW";
    public static final String DOC_CLAIM           = "DOC_CLAIM";
    public static final String DOC_VIEW_DETAILS    = "DOC_VIEW_DETAILS";
    public static final String DOC_APPROVE         = "DOC_APPROVE";
    public static final String DOC_REJECT          = "DOC_REJECT";

    // Optional legacy
    public static final String DOC_VERIFY          = "DOC_VERIFY";

    // ============================================================
    // ALERT PERMISSIONS
    // ============================================================
    public static final String ALERT_VIEW                  = "ALERT_VIEW";
    public static final String ALERT_CLAIM                 = "ALERT_CLAIM";
    public static final String ALERT_DISMISS               = "ALERT_DISMISS";
    public static final String ALERT_CONVERT_TO_CASE       = "ALERT_CONVERT_TO_CASE";

    public static final String SCREENING_CANDIDATE         = "SCREENING_CANDIDATE";

    // ============================================================
    // CASE PERMISSIONS
    // ============================================================
    public static final String CASE_VIEW                   = "CASE_VIEW";
    public static final String CASE_VIEW_ESCALATED         = "CASE_VIEW_ESCALATED";
    public static final String CASE_CREATE                 = "CASE_CREATE";
    public static final String CASE_CLAIM                  = "CASE_CLAIM";
    public static final String CASE_ADD_NOTE               = "CASE_ADD_NOTE";
    public static final String CASE_UPDATE_STATUS          = "CASE_UPDATE_STATUS";
    public static final String CASE_ESCALATE               = "CASE_ESCALATE";
    public static final String CASE_RESOLVE                = "CASE_RESOLVE";
    public static final String CASE_ASSIGN_OFFICER         = "CASE_ASSIGN_OFFICER";

    // ============================================================
    // WATCHLIST
    // ============================================================
    public static final String WATCHLIST_VIEW              = "WATCHLIST_VIEW";
    public static final String WATCHLIST_EVIDENCE_VIEW     = "WATCHLIST_EVIDENCE_VIEW";
    public static final String WATCHLIST_CREATE            = "WATCHLIST_CREATE";
    public static final String WATCHLIST_EDIT              = "WATCHLIST_EDIT";
    public static final String WATCHLIST_SOFT_DELETE       = "WATCHLIST_SOFT_DELETE";

    // ============================================================
    // USER / ADMIN
    // ============================================================
    public static final String USER_CREATE                 = "USER_CREATE";
    public static final String USER_VIEW_ALL               = "USER_VIEW_ALL";
    public static final String USER_ACTIVATE_DEACTIVATE    = "USER_ACTIVATE_DEACTIVATE";

    // ============================================================
    // SYSTEM
    // ============================================================
    public static final String SCREENING_CONFIG_SWITCH     = "SCREENING_CONFIG_SWITCH";
    public static final String SCREENING_RESCREEN          = "SCREENING_RESCREEN";
    public static final String REPORTS_VIEW                = "REPORTS_VIEW";
    public static final String AUDIT_LOG_VIEW              = "AUDIT_LOG_VIEW";
    public static final String SYSTEM_CONFIG_MANAGE        = "SYSTEM_CONFIG_MANAGE";

    // ============================================================
    // APPROVAL FLOW
    // ============================================================
    public static final String APPROVAL_CREATE             = "APPROVAL_CREATE";
    public static final String APPROVAL_APPROVE_REJECT     = "APPROVAL_APPROVE_REJECT";
    public static final String APPROVAL_BYPASS             = "APPROVAL_BYPASS";

    // ============================================================
    // SUPER ADMIN
    // ============================================================
    public static final String ROLE_MANAGE                 = "ROLE_MANAGE";

    // ============================================================
    // ROLE -> PERMISSION MAPPING
    // ============================================================
    public static Set<String> getPermissions(RoleCode role) {
        return switch (role) {

            case ROLE_CANDIDATE -> Set.of(
                    CANDIDATE_FORM_SUBMIT,
                    CANDIDATE_DOC_UPLOAD,
                    CANDIDATE_STATUS_VIEW_OWN
            );

            case ROLE_OFFICER_L1 -> Set.of(
                    ALERT_VIEW, ALERT_CLAIM, ALERT_DISMISS, ALERT_CONVERT_TO_CASE,

                    CASE_VIEW,
                    CASE_CREATE,
                    CASE_CLAIM,
                    CASE_ADD_NOTE,
                    CASE_UPDATE_STATUS,
                    CASE_ESCALATE,
                    // Note: L1 explicitly does NOT get CASE_VIEW_ESCALATED

                    DOC_QUEUE_VIEW, DOC_CLAIM, DOC_VIEW_DETAILS, DOC_APPROVE, DOC_REJECT, DOC_VERIFY,
                    SCREENING_CANDIDATE,
                    WATCHLIST_VIEW, WATCHLIST_EVIDENCE_VIEW
            );

            case ROLE_OFFICER_L2 -> Set.of(
                    ALERT_VIEW,

                    CASE_VIEW,
                    CASE_VIEW_ESCALATED, // 🔥 ADDED HERE
                    CASE_CREATE,
                    CASE_CLAIM,
                    CASE_ADD_NOTE,
                    CASE_UPDATE_STATUS,
                    CASE_ESCALATE,
                    CASE_RESOLVE,

                    DOC_QUEUE_VIEW, DOC_CLAIM, DOC_VIEW_DETAILS, DOC_APPROVE, DOC_REJECT, DOC_VERIFY,
                    WATCHLIST_VIEW, WATCHLIST_EVIDENCE_VIEW
            );

            case ROLE_ADMIN -> Set.of(
                    ALERT_VIEW, ALERT_CLAIM, ALERT_DISMISS, ALERT_CONVERT_TO_CASE,

                    CASE_VIEW, CASE_VIEW_ESCALATED, CASE_CREATE, CASE_CLAIM, CASE_ADD_NOTE,
                    CASE_UPDATE_STATUS, CASE_ESCALATE, CASE_RESOLVE, CASE_ASSIGN_OFFICER,

                    DOC_QUEUE_VIEW, DOC_CLAIM, DOC_VIEW_DETAILS, DOC_APPROVE, DOC_REJECT, DOC_VERIFY,
                    WATCHLIST_VIEW, WATCHLIST_EVIDENCE_VIEW, WATCHLIST_CREATE, WATCHLIST_EDIT, WATCHLIST_SOFT_DELETE,
                    USER_CREATE, USER_VIEW_ALL, USER_ACTIVATE_DEACTIVATE,
                    SCREENING_CONFIG_SWITCH, SCREENING_RESCREEN, REPORTS_VIEW, AUDIT_LOG_VIEW, SYSTEM_CONFIG_MANAGE,
                    APPROVAL_CREATE
            );

            case ROLE_SUPER_ADMIN -> Set.of(
                    ALERT_VIEW, ALERT_CLAIM, ALERT_DISMISS, ALERT_CONVERT_TO_CASE,

                    CASE_VIEW, CASE_VIEW_ESCALATED, CASE_CREATE, CASE_CLAIM, CASE_ADD_NOTE,
                    CASE_UPDATE_STATUS, CASE_ESCALATE, CASE_RESOLVE, CASE_ASSIGN_OFFICER,

                    DOC_QUEUE_VIEW, DOC_CLAIM, DOC_VIEW_DETAILS, DOC_APPROVE, DOC_REJECT, DOC_VERIFY,
                    WATCHLIST_VIEW, WATCHLIST_EVIDENCE_VIEW, WATCHLIST_CREATE, WATCHLIST_EDIT, WATCHLIST_SOFT_DELETE,
                    USER_CREATE, USER_VIEW_ALL, USER_ACTIVATE_DEACTIVATE,
                    SCREENING_CONFIG_SWITCH, SCREENING_RESCREEN, REPORTS_VIEW, AUDIT_LOG_VIEW, SYSTEM_CONFIG_MANAGE,
                    APPROVAL_CREATE, APPROVAL_APPROVE_REJECT, APPROVAL_BYPASS, ROLE_MANAGE
            );
        };
    }

    public static boolean isStaff(RoleCode role) {
        return role != RoleCode.ROLE_CANDIDATE;
    }
}