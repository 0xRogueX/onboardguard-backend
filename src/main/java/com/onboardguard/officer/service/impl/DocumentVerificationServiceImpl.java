package com.onboardguard.officer.service.impl;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.entity.CandidateDocument;
import com.onboardguard.candidate.enums.DocumentStatus;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.candidate.repository.CandidateDocumentRepository;
import com.onboardguard.candidate.repository.CandidateRepository;
import com.onboardguard.candidate.service.CandidateDocumentService;
import com.onboardguard.officer.dto.CandidateQueueItemDto;
import com.onboardguard.officer.dto.CandidateVerificationDashboardDto;
import com.onboardguard.officer.mapper.OfficerCandidateMapper;
import com.onboardguard.officer.service.DocumentVerificationService;
import com.onboardguard.screening.service.ScreeningOrchestrationService;
import com.onboardguard.shared.common.events.DocumentRejectedEvent;
import com.onboardguard.shared.common.events.DocumentVerificationCompletedEvent;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
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
public class DocumentVerificationServiceImpl implements DocumentVerificationService {

    private final CandidateDocumentRepository documentRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateDocumentService candidateDocumentService;
    private final OfficerCandidateMapper officerCandidateMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ScreeningOrchestrationService screeningOrchestrationService;

    /**
     * Statuses that mean a candidate is waiting in the officer queue.
     * FORM_SUBMITTED = candidate hit "Submit" button.
     * DOCUMENTS_UNDER_REVIEW = previously claimed but lock was released (e.g. officer session expired).
     */
    private static final List<OnboardingStatus> QUEUE_STATUSES = List.of(
            OnboardingStatus.FORM_SUBMITTED,
            OnboardingStatus.DOCUMENTS_UNDER_REVIEW
    );

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getCandidateDocumentsForReview(Long candidateId) {
        return documentRepository.findByCandidateId(candidateId)
                .stream()
                .map(candidateDocumentService::mapToResponseWithUrl)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('DOC_QUEUE_VIEW')")
    public List<CandidateQueueItemDto> getPendingCandidatesQueue(Long officerId) {
        List<Candidate> pendingCandidates = candidateRepository
                .findAvailableOrClaimedByMe(QUEUE_STATUSES, officerId);

        log.info("Officer queue loaded: {} candidates waiting", pendingCandidates.size());

        return pendingCandidates.stream()
                .map(officerCandidateMapper::toQueueItemDto)
                .toList();
    }

    //MANUAL PULL: Officer clicks a specific candidate in the grid to lock and claim them. Transitions status to DOCUMENTS_UNDER_REVIEW so it's visible on the candidate portal.
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('DOC_CLAIM')")
    public void claimCandidateForVerification(Long candidateId, Long officerId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));

        // Validate candidate is in a claimable state
        if (!QUEUE_STATUSES.contains(candidate.getOnboardingStatus())) {
            throw new BadRequestException("Candidate is not in a state that can be claimed. Current status: " + candidate.getOnboardingStatus());
        }

        if (candidate.getVerificationLockedBy() != null && !candidate.getVerificationLockedBy().equals(officerId)) {
            throw new BadRequestException("This candidate is already being reviewed by Officer #" + candidate.getVerificationLockedBy());
        }

        lockCandidate(candidate, officerId);
    }

    // AUTO PUSH (FIFO): Automatically finds the oldest unlocked candidate, locks it, and returns the dashboard.
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('DOC_CLAIM')")
    public CandidateVerificationDashboardDto claimNextAvailableCandidate(Long officerId) {
        Candidate nextCandidate = candidateRepository
                .findFirstAvailableForVerification(QUEUE_STATUSES)
                .orElseThrow(() -> new ResourceNotFoundException("No candidates currently waiting for verification!"));

        lockCandidate(nextCandidate, officerId);

        return getCandidateVerificationDetails(nextCandidate.getId());
    }


    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('DOC_VIEW_DETAILS')")
    public CandidateVerificationDashboardDto getCandidateVerificationDetails(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));

        List<DocumentResponseDto> documents = documentRepository.findByCandidateId(candidateId)
                .stream()
                .map(candidateDocumentService::mapToResponseWithUrl)
                .toList();

        return officerCandidateMapper.toDashboardDto(candidate, documents);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('DOC_APPROVE')")
    public void approveDocument(Long documentId, Long officerId) {
        CandidateDocument document = getDocumentById(documentId);
        validateLockOwnership(document.getCandidate(), officerId);

        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new BadRequestException("Document is already verified.");
        }

        document.setStatus(DocumentStatus.VERIFIED);
        document.setVerifiedBy(officerId);
        document.setVerifiedAt(Instant.now());
        document.setRejectionReason(null);

        documentRepository.save(document);
        log.info("Document ID {} VERIFIED by Officer ID {}", documentId, officerId);

        checkAndAdvanceCandidateStatus(document.getCandidate().getId(), officerId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('DOC_REJECT')")
    public void rejectDocument(Long documentId, String reason, Long officerId) {
        CandidateDocument document = getDocumentById(documentId);
        validateLockOwnership(document.getCandidate(), officerId);

        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new BadRequestException("Cannot reject a document that has already been verified.");
        }

        document.setStatus(DocumentStatus.REJECTED);
        document.setRejectionReason(reason);
        document.setVerifiedBy(officerId);
        document.setVerifiedAt(Instant.now());
        documentRepository.save(document);

        Candidate candidate = document.getCandidate();

        // Mark as rejected but DO NOT release lock yet so officer can finish reviewing other docs
        candidate.setOnboardingStatus(OnboardingStatus.DOCUMENTS_REJECTED);
        // candidate.setVerificationLockedBy(null);
        // candidate.setVerificationLockedAt(null);
        candidateRepository.save(candidate);

        log.info("Document ID {} REJECTED by Officer ID {}. Reason: {}", documentId, officerId, reason);

        // Fire event to email the candidate
        DocumentRejectedEvent event = new DocumentRejectedEvent(
                candidate.getUser().getEmail(),
                candidate.getPersonalDetail().getFirstName() + " " + candidate.getPersonalDetail().getLastName(),
                document.getCandidateDocumentType().name(),
                reason
        );
        eventPublisher.publishEvent(event);
    }

    private void lockCandidate(Candidate candidate, Long officerId) {
        candidate.setVerificationLockedBy(officerId);
        candidate.setVerificationLockedAt(Instant.now());
        // Transition to DOCUMENTS_UNDER_REVIEW so candidate portal reflects it
        candidate.setOnboardingStatus(OnboardingStatus.DOCUMENTS_UNDER_REVIEW);
        candidateRepository.save(candidate);

        log.info("Candidate ID {} locked by Officer ID {} — status → DOCUMENTS_UNDER_REVIEW", candidate.getId(), officerId);
    }

    private void validateLockOwnership(Candidate candidate, Long officerId) {
        if (!officerId.equals(candidate.getVerificationLockedBy())) {
            throw new BadRequestException("You cannot modify documents for a candidate you have not claimed. Claim this candidate first.");
        }
    }

    private CandidateDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
    }

    private void checkAndAdvanceCandidateStatus(Long candidateId, Long officerId) {
        List<CandidateDocument> allDocs = documentRepository.findByCandidateId(candidateId);

        if (allDocs.isEmpty()) {
            log.warn("Candidate ID {} has no documents — cannot advance status.", candidateId);
            return;
        }

        boolean allVerified = allDocs.stream().allMatch(doc -> doc.getStatus() == DocumentStatus.VERIFIED);

        if (allVerified) {
            Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

            // NEW STATUS: DOCUMENTS_VERIFIED — candidate portal will show this
            candidate.setOnboardingStatus(OnboardingStatus.DOCUMENTS_VERIFIED);

            // Release the lock — officer has finished their job
            candidate.setVerificationLockedBy(null);
            candidate.setVerificationLockedAt(null);
            candidateRepository.save(candidate);

            log.info("All documents for Candidate ID {} VERIFIED by Officer ID {}. Triggering screening engine.", candidateId, officerId);

            eventPublisher.publishEvent(new DocumentVerificationCompletedEvent(candidateId));

            // Hand off to screening engine (which will set to SCREENING_IN_PROGRESS)
            screeningOrchestrationService.runScreening(candidateId);
        } else {
            long pendingCount = allDocs.stream().filter(d -> d.getStatus() == DocumentStatus.PENDING).count();
            log.info("Candidate ID {} still has {} document(s) pending review.", candidateId, pendingCount);
        }
    }
}