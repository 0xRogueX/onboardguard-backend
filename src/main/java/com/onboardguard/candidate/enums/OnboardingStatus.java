package com.onboardguard.candidate.enums;

public enum OnboardingStatus {
    REGISTERED,
    PERSONAL_SAVED,
    PROFESSIONAL_SAVED,
    DOCUMENTS_UPLOADED,
    FORM_SUBMITTED,          // Candidate submitted the form — awaits officer queue
    DOCUMENTS_UNDER_REVIEW,  // Officer has claimed and is actively reviewing
    DOCUMENTS_VERIFIED,      // All documents approved by officer — triggers screening
    DOCUMENTS_REJECTED,      // One or more documents were rejected — awaits re-upload
    SCREENING_PENDING,       // Handed off to screening engine
    SCREENING_IN_PROGRESS,   // Screening engine actively running
    SCREENING_CLEARED,       // Screening passed — no alerts
    CASE_IN_REVIEW,          // Alert converted to case — L1 investigating
    FLAGGED,                 // Case escalated to L2
    APPROVED,                // L2 approved — candidate can onboard
    REJECTED                 // L2 rejected — candidate blocked
}