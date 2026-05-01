package com.onboardguard.officer.service.impl;

import com.onboardguard.officer.dto.CaseDetailDto;
import com.onboardguard.officer.dto.EscalateCaseDto;
import com.onboardguard.officer.dto.ResolveCaseDto;
import com.onboardguard.officer.entity.Case;
import com.onboardguard.officer.entity.CaseNote;
import com.onboardguard.officer.mapper.CaseMapper;
import com.onboardguard.officer.repository.CaseRepository;
import com.onboardguard.officer.service.CaseService;
import com.onboardguard.shared.common.enums.CaseStatus;
import com.onboardguard.shared.common.enums.NoteType;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import com.onboardguard.shared.common.exception.UnauthorizedAccessException;
import com.onboardguard.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;

    // 1. DASHBOARD QUEUES
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CASE_VIEW')")
    public List<CaseDetailDto> getAvailableCasesForQueue() {
        // Include both globally unassigned OPEN cases and cases already assigned to the current officer
        Long currentOfficerId = securityUtils.getCurrentUserPrincipal().getUserId();
        return caseRepository.findAvailableCasesForQueueIncludingOwned(CaseStatus.OPEN, currentOfficerId)
                .stream()
                .map(caseMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CASE_VIEW_ESCALATED')")
    public List<CaseDetailDto> getEscalatedCasesQueue() {
        return caseRepository.findEscalatedCasesForL2Queue(CaseStatus.ESCALATED)
                .stream()
                .map(caseMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CASE_VIEW')")
    public CaseDetailDto getCaseDetails(Long caseId) {
        Case c = getCaseById(caseId);
        Long userId = securityUtils.getCurrentUserPrincipal().getUserId();

        if (c.getAssignedOfficerId() != null && !c.getAssignedOfficerId().equals(userId)) {
            throw new UnauthorizedAccessException("You cannot access this case. It is assigned to another officer.");
        }
        return caseMapper.toDto(c);
    }


    // 2. L1 OFFICER ACTIONS (OPEN -> IN_REVIEW -> ESCALATED)
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_CLAIM')")
    public void claimOpenCaseManual(Long caseId, Long l1OfficerId) {
        Case investigationCase = caseRepository.findByIdForUpdate(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found"));

        if (investigationCase.getStatus() != CaseStatus.OPEN) {
            throw new IllegalStateException("Only OPEN cases can be claimed.");
        }
        if (investigationCase.getAssignedOfficerId() != null) {
            throw new IllegalStateException("Case already claimed by another officer.");
        }

        // State Transition
        investigationCase.setStatus(CaseStatus.IN_REVIEW);
        investigationCase.setAssignedOfficerId(l1OfficerId);
        investigationCase.setAssignedAt(Instant.now());

        caseRepository.save(investigationCase);
        log.info("Case ID {} MANUALLY claimed by L1 Officer {}. Status -> IN_REVIEW.", caseId, l1OfficerId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_CLAIM')")
    public CaseDetailDto claimNextOpenCaseFifo(Long l1OfficerId) {
        Case investigationCase = caseRepository.findFirstNextOpenCaseForUpdate(CaseStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No open cases available in the queue."));

        // State Transition
        investigationCase.setStatus(CaseStatus.IN_REVIEW);
        investigationCase.setAssignedOfficerId(l1OfficerId);
        investigationCase.setAssignedAt(Instant.now());

        log.info("Case ID {} FIFO claimed by L1 Officer {}. Status -> IN_REVIEW.", investigationCase.getId(), l1OfficerId);
        return caseMapper.toDto(caseRepository.save(investigationCase));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_ESCALATE')")
    public void escalateCase(Long caseId, EscalateCaseDto dto, Long l1OfficerId) {
        Case investigationCase = getCaseById(caseId);

        if (investigationCase.getStatus() != CaseStatus.IN_REVIEW) {
            throw new IllegalStateException("Only IN_REVIEW cases can be escalated.");
        }
        validateCaseOwnership(investigationCase, l1OfficerId);

        // State Transition
        investigationCase.setStatus(CaseStatus.ESCALATED);
        investigationCase.setEscalatedTo(dto.escalatedTo());
        investigationCase.setEscalatedAt(Instant.now());
        investigationCase.setEscalationReason(dto.escalationReason());
        investigationCase.setEscalatedBy(l1OfficerId);
        investigationCase.setAssignedOfficerId(null); // Unlock so L2 can see it

        CaseNote escalationNote = CaseNote.builder()
                .investigationCase(investigationCase)
                .authorId(l1OfficerId)
                .content("ESCALATION MEMO: " + dto.escalationReason())
                .noteType(NoteType.ESCALATION)
                .build();

        investigationCase.getNotes().add(escalationNote);
        caseRepository.save(investigationCase);
        log.info("Case ID {} ESCALATED by L1 Officer {}", caseId, l1OfficerId);
    }


    // 3. L2 OFFICER ACTIONS (ESCALATED -> RESOLVED)
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_RESOLVE')")
    public void claimEscalatedCaseManual(Long caseId, Long l2OfficerId) {
        Case investigationCase = caseRepository.findByIdForUpdate(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found"));

        if (investigationCase.getStatus() != CaseStatus.ESCALATED) {
            throw new IllegalStateException("Only ESCALATED cases can be claimed by an L2 Checker.");
        }
        if (investigationCase.getAssignedOfficerId() != null) {
            throw new IllegalStateException("Case already claimed by another officer.");
        }

        // Lock to L2 Officer (stays ESCALATED, but drops off dashboard due to ID assignment)
        investigationCase.setAssignedOfficerId(l2OfficerId);
        investigationCase.setAssignedAt(Instant.now());
        caseRepository.save(investigationCase);
        log.info("ESCALATED Case ID {} MANUALLY claimed by L2 Officer {}", caseId, l2OfficerId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_RESOLVE')")
    public CaseDetailDto claimNextEscalatedCaseFifo(Long l2OfficerId) {
        Case investigationCase = caseRepository.findFirstNextEscalatedCaseForUpdate(CaseStatus.ESCALATED)
                .orElseThrow(() -> new ResourceNotFoundException("No escalated cases available in the queue."));

        investigationCase.setAssignedOfficerId(l2OfficerId);
        investigationCase.setAssignedAt(Instant.now());

        log.info("ESCALATED Case ID {} FIFO claimed by L2 Officer {}", investigationCase.getId(), l2OfficerId);
        return caseMapper.toDto(caseRepository.save(investigationCase));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('CASE_RESOLVE')")
    public void resolveCase(Long caseId, ResolveCaseDto dto, Long l2OfficerId) {
        Case investigationCase = getCaseById(caseId);

        if (investigationCase.getStatus() != CaseStatus.ESCALATED) {
            throw new IllegalStateException("Case must be in ESCALATED state to be resolved.");
        }
        validateCaseOwnership(investigationCase, l2OfficerId);

        // Final State Transition
        investigationCase.setStatus(CaseStatus.RESOLVED);
        investigationCase.setOutcome(dto.outcome());
        investigationCase.setOutcomeReason(dto.outcomeReason());
        investigationCase.setResolvedBy(l2OfficerId);
        investigationCase.setResolvedAt(Instant.now());

        CaseNote resolutionNote = CaseNote.builder()
                .investigationCase(investigationCase)
                .authorId(l2OfficerId)
                .content("FINAL DECISION (" + dto.outcome().name() + "): " + dto.outcomeReason())
                .noteType(NoteType.SYSTEM_ACTION)
                .build();

        investigationCase.getNotes().add(resolutionNote);
        caseRepository.save(investigationCase);
        log.info("Case ID {} RESOLVED with outcome {} by L2 Officer {}", caseId, dto.outcome(), l2OfficerId);
    }


    private Case getCaseById(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));
    }

    private void validateCaseOwnership(Case investigationCase, Long officerId) {
        if (!officerId.equals(investigationCase.getAssignedOfficerId())) {
            throw new UnauthorizedAccessException("You cannot perform this action because the case is locked by another officer.");
        }
    }
}