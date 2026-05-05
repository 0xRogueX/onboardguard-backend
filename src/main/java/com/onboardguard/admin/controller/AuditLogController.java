package com.onboardguard.admin.controller;

import com.onboardguard.admin.dto.AuditLogDto;
import com.onboardguard.admin.service.AuditLogService;
import com.onboardguard.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getTimeline(
            @RequestParam String entityType,
            @RequestParam Long entityId) {

        List<AuditLogDto> timeline = auditLogService.getEntityHistory(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Timeline fetched successfully", timeline));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long performedBy,
            Pageable pageable) {

        log.info("Fetching Audit Logs with filters - EntityType: {}, Action: {}, PerformedBy: {}", entityType, action, performedBy);
        Page<AuditLogDto> logs = auditLogService.getAllAuditLogs(entityType, action, performedBy, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", logs));
    }
}