package com.onboardguard.auth.service.impl;

import com.onboardguard.auth.dto.request.CreateOfficerDto;
import com.onboardguard.auth.dto.request.LoginRequestDto;
import com.onboardguard.auth.dto.request.RegisterCandidateDto;
import com.onboardguard.auth.dto.response.CandidateLoginResponseDto;
import com.onboardguard.auth.dto.response.StaffLoginResponseDto;
import com.onboardguard.auth.entity.AppUser;
import com.onboardguard.auth.mapper.AuthMapper;
import com.onboardguard.auth.repository.AppUserRepository;
import com.onboardguard.auth.service.AuthService;
import com.onboardguard.auth.service.OfficerCredentialGenerator;
import com.onboardguard.shared.common.enums.RoleCode;
import com.onboardguard.shared.common.events.CandidateRegisteredEvent;
import com.onboardguard.shared.common.events.OfficerCreatedEvent;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.security.CustomUserDetails;
import com.onboardguard.shared.security.CustomUserDetailsService;
import com.onboardguard.shared.security.JwtTokenProvider;
import com.onboardguard.shared.security.SecurityConstants;
import com.onboardguard.shared.security.SecurityUtils;
import com.onboardguard.shared.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;
    private final OfficerCredentialGenerator credentialGenerator;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityUtils securityUtils;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public CandidateLoginResponseDto loginCandidate(LoginRequestDto dto) {
        Authentication auth = doAuthenticate(dto.email(), dto.password());
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        if (!principal.isCandidate()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(auth);
        updateLastLogin(dto.email());
        log.info("Candidate login: email={}", dto.email());

        return authMapper.toCandidateDto(principal, token, jwtExpirationMs / 1000);
    }

    @Override
    @Transactional
    public StaffLoginResponseDto loginStaff(LoginRequestDto dto) {
        Authentication auth = doAuthenticate(dto.email(), dto.password());
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        if (principal.isCandidate()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(auth);
        updateLastLogin(dto.email());
        log.info("Staff login: email={} role={}", dto.email(), principal.getRole().name());

        return authMapper.toStaffDto(principal, token, principal.getRole().name(), jwtExpirationMs / 1000);
    }

    @Override
    @Transactional
    public CandidateLoginResponseDto registerCandidate(RegisterCandidateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("Email already registered");
        }

        AppUser user = authMapper.toEntity(dto, passwordEncoder.encode(dto.password()));
        user.setLastLoginAt(Instant.now());
        AppUser saved = userRepository.save(user);

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        String token = jwtTokenProvider.generateTokenForUser(saved);

        eventPublisher.publishEvent(
                new CandidateRegisteredEvent(
                        saved.getEmail(),
                        saved.getFullName())
        );

        log.info("Candidate registered: email={}", saved.getEmail());

        return authMapper.toCandidateDto(saved, token, jwtExpirationMs / 1000);
    }

    @Override
    @Transactional
    public void createOfficer(CreateOfficerDto dto, AppUser createdBy) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("An account with this email already exists.");
        }

        if (!Set.of(RoleCode.ROLE_OFFICER_L1, RoleCode.ROLE_OFFICER_L2).contains(dto.role())) {
            log.warn("SECURITY ALERT: User {} tried to create invalid role {}", createdBy.getId(), dto.role());
            throw new SecurityException("Only L1 and L2 officers allowed");
        }

//        String rawPassword = credentialGenerator.generatePassword();
        String rawPassword = "password123";

        AppUser officer = authMapper.toEntity(
                dto,
                passwordEncoder.encode(rawPassword),
                createdBy
        );

        userRepository.save(officer);

        log.info("AUDIT: Officer created | createdBy={} | newUser={} | role={}",
                createdBy.getId(), officer.getId(), officer.getRole());

        eventPublisher.publishEvent(
                new OfficerCreatedEvent(
                        officer.getEmail(),
                        officer.getFullName(),
                        rawPassword,
                        officer.getRole().name(),
                        createdBy.getEmail()
                )
        );
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new BadRequestException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired token");
        }

        String email = jwtTokenProvider.getUsername(token);

        blacklistService.blacklist(token);
        userDetailsService.evictCache(email);

        log.info("Logout successful: email={}", email);
    }

    private Authentication doAuthenticate(String email, String password) {
        try {
            return authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new DisabledException("Account is deactivated");
        } catch (LockedException e) {
            throw new LockedException("Account is locked");
        }
    }

    private void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setLastLoginAt(Instant.now());
            userRepository.save(u);
            userDetailsService.evictCache(email);
        });
    }
}