package com.onboardguard.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardReportDto {

    private CandidateStats candidateStats;
    private AlertStats alertStats;
    private CasePerformanceStats casePerformanceStats;
    private Map<String, Long> categoryHitFrequency; // e.g., {"SANCTIONS": 45, "FRAUD": 12}

    @Data
    @Builder
    public static class CandidateStats {
        private long totalOnboarded;
        private long pendingScreening;
        private long cleared;
        private long flagged;
    }

    @Data
    @Builder
    public static class AlertStats {
        private long totalGenerated;
        private long openAlerts;
        private long dismissedFalsePositives;
        private long escalatedToCases;
    }

    @Data
    @Builder
    public static class CasePerformanceStats {
        private long totalOpenCases;
        private long totalResolvedCases;
        private double averageResolutionTimeHours; // SLA tracking
        private long slaBreachedCases; // Cases that took too long
    }
}