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
import com.onboardguard.shared.common.events.CandidateRegisteredEvent;
import com.onboardguard.shared.security.CustomUserDetails;
import com.onboardguard.shared.security.CustomUserDetailsService;
import com.onboardguard.shared.security.JwtTokenProvider;
import com.onboardguard.shared.security.SecurityConstants;
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

        // Manual instantiation here is optimal: avoids hitting the DB to convert CustomUserDetails back to AppUser
        return new CandidateLoginResponseDto(
                token, principal.getEmail(), principal.getFullName(), jwtExpirationMs / 1000);
    }

    @Override
    public StaffLoginResponseDto loginStaff(LoginRequestDto dto) {
        Authentication auth = doAuthenticate(dto.email(), dto.password());
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        if (principal.isCandidate()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(auth);
        String roleCode = principal.getRole().name();
        updateLastLogin(dto.email());

        log.info("Staff login: email={} role={}", dto.email(), roleCode);

        // Manual instantiation here is optimal: avoids hitting the DB to convert CustomUserDetails back to AppUser
        return new StaffLoginResponseDto(
                token, principal.getEmail(), principal.getFullName(), roleCode, jwtExpirationMs / 1000);
    }

    @Override
    @Transactional
    public CandidateLoginResponseDto registerCandidate(RegisterCandidateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        AppUser user = authMapper.toCandidateEntity(dto);

        String username = dto.email().split("@")[0];
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis() % 10000;
        }

        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        AppUser saved = userRepository.save(user);
        String token = jwtTokenProvider.generateTokenForUser(saved);

        eventPublisher.publishEvent(new CandidateRegisteredEvent(saved.getEmail(), saved.getFullName()));

        log.info("Candidate registered: email={}", saved.getEmail());

        // Fully utilizing the mapper for response mapping
        return authMapper.toCandidateResponse(saved, token, jwtExpirationMs / 1000);
    }

    @Override
    @Transactional
    public void createOfficer(CreateOfficerDto dto, AppUser createdBy) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already registered: " + dto.email());
        }

        String plainUsername = credentialGenerator.generateUsername(dto.fullName());
        String plainPassword = credentialGenerator.generatePassword();

        // Fully utilizing the mapper for entity mapping (role, active, locked are implicitly set by Mapper)
        AppUser officer = authMapper.toOfficerEntity(dto);
        officer.setPasswordHash(passwordEncoder.encode(plainPassword));
        officer.setCreatedBy(createdBy);

        userRepository.save(officer);

        log.info("Officer created: email={} username={} by={}",
                officer.getEmail(), plainUsername, createdBy.getEmail());
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
        String email = jwtTokenProvider.getUsername(token);

        blacklistService.blacklist(token);
        userDetailsService.evictCache(email);

        log.info("Logout successful: email={}", email);
    }

    private Authentication doAuthenticate(String email, String password) {
        try {
            return authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
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