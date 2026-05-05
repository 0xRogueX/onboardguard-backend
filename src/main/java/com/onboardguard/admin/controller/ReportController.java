package com.onboardguard.admin.controller;

import com.onboardguard.admin.dto.DashboardReportDto;
import com.onboardguard.admin.service.ReportService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardReportDto>> getDashboardStats() {

        String currentUserEmail = securityUtils.getCurrentUserPrincipal().getEmail();
        log.info("User {} is fetching the dashboard statistics", currentUserEmail);

        // Fetch the data (Will hit Redis instantly, or run SQL if cache expired)
        DashboardReportDto dashboardData = reportService.generateDashboard();

        return ResponseEntity.ok(ApiResponse.success(
                "Dashboard statistics retrieved successfully",
                dashboardData
        ));
    }
}