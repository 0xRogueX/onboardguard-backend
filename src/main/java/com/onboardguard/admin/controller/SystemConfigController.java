package com.onboardguard.admin.controller;

import com.onboardguard.admin.dto.SystemConfigResponseDto;
import com.onboardguard.admin.dto.UpdateSystemConfigDto;
import com.onboardguard.admin.service.SystemConfigAdminService;
import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.common.enums.RoleCode;
import com.onboardguard.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.onboardguard.shared.security.RolePermissions;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigAdminService systemConfigAdminService;
    private final SecurityUtils securityUtils;

    @PutMapping("/{configId}")
    @PreAuthorize("hasAuthority('SYSTEM_CONFIG_MANAGE')")
    public ResponseEntity<ApiResponse<String>> requestConfigUpdate(
            @PathVariable Long configId,
            @Valid @RequestBody UpdateSystemConfigDto updateDto) {

        var principal = securityUtils.getCurrentUserPrincipal();
        if (principal == null) {
            log.warn("Unauthenticated request to requestConfigUpdate");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Unauthorized", "UNAUTHORIZED"));
        }
        Long currentUserId = principal.getUserId();
        RoleCode currentUserRole = principal.getRole();

        log.info("Admin ID {} is submitting an update request for config ID {}", currentUserId, configId);

        systemConfigAdminService.requestConfigUpdate(configId, updateDto, currentUserId, currentUserRole);

        return ResponseEntity.ok(ApiResponse.success("Configuration update request submitted successfully and is pending Super Admin approval.", null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_CONFIG_MANAGE')")
    public ResponseEntity<ApiResponse<List<SystemConfigResponseDto>>> getAllConfigs() {

        var principal = securityUtils.getCurrentUserPrincipal();
        String actor = principal == null ? "UNKNOWN" : principal.getEmail();

        log.info("Admin {} is viewing the system configuration grid", actor);

        List<SystemConfigResponseDto> configs = systemConfigAdminService.getAllConfigs();

        return ResponseEntity.ok(ApiResponse.success("Fetched all the configurations", configs));
    }
}