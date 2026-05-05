package com.onboardguard.admin.dto;

import lombok.Builder;
import java.io.Serializable;
import java.util.Map;

@Builder(toBuilder = true)
public record DashboardReportDto(
        CandidateStats candidateStats,
        AlertStats alertStats,
        CasePerformanceStats casePerformanceStats,

        // e.g., {"SANCTIONS": 45, "FRAUD": 12, "PEP": 8}
        Map<String, Long> categoryHitFrequency,

        // Essential for the Super Admin to know if they have pending approvals
        long pendingMakerCheckerRequests
) implements Serializable {

    @Builder(toBuilder = true)
    public record CandidateStats(
            long totalOnboarded,
            long pendingScreening,
            long cleared,
            long flagged
    ) implements Serializable {}

    @Builder(toBuilder = true)
    public record AlertStats(
            long totalGenerated,
            long openAlerts,
            long dismissedFalsePositives,
            long escalatedToCases
    ) implements Serializable {}

    @Builder(toBuilder = true)
    public record CasePerformanceStats(
            long totalOpenCases,
            long totalResolvedCases,
            double averageResolutionTimeHours, // For SLA tracking
            long slaBreachedCases              // Cases that breached the SLA timeline
    ) implements Serializable {}
}