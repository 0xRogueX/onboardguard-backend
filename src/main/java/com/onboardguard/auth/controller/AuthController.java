package com.onboardguard.auth.controller;

import com.onboardguard.auth.dto.request.CreateOfficerDto;
import com.onboardguard.auth.dto.request.LoginRequestDto;
import com.onboardguard.auth.dto.request.RegisterCandidateDto;
import com.onboardguard.auth.dto.response.CandidateLoginResponseDto;
import com.onboardguard.auth.dto.response.StaffLoginResponseDto;
import com.onboardguard.auth.service.AuthService;
import com.onboardguard.shared.security.RolePermissions;
import com.onboardguard.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    // ── CANDIDATE LOGIN (External Portal — public) ────────────────────────
    @PostMapping("/login/candidate")
    public ResponseEntity<CandidateLoginResponseDto> loginCandidate(
            @Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.loginCandidate(dto));
    }

    // ── STAFF LOGIN (Internal Portal — Officer/Admin/Super Admin — public) ─
    @PostMapping("/login/staff")
    public ResponseEntity<StaffLoginResponseDto> loginStaff(
            @Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.loginStaff(dto));
    }

    // ── CANDIDATE SELF-REGISTRATION (public) ──────────────────────────────
    @PostMapping("/register/candidate")
    public ResponseEntity<CandidateLoginResponseDto> registerCandidate(
            @Valid @RequestBody RegisterCandidateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerCandidate(dto));
    }

    // ── LOGOUT (requires valid JWT) ───────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    // ── OFFICER CREATION (Admin module — placed here for completion context) ─
    @PostMapping("/admin/officer")
    @PreAuthorize("hasAuthority('" + RolePermissions.USER_CREATE + "')")
    public ResponseEntity<Void> createOfficer(@Valid @RequestBody CreateOfficerDto dto) {
        // AppUser uses UUID id, securityUtils resolves full entity mapping automatically via its token principal
        authService.createOfficer(dto, securityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}