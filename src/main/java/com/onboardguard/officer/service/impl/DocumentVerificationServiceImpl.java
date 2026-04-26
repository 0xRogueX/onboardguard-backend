package com.onboardguard.officer.service.impl;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.entity.CandidateDocument;
import com.onboardguard.candidate.enums.DocumentStatus;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.candidate.mapper.CandidateMapper;
import com.onboardguard.candidate.repository.CandidateDocumentRepository;
import com.onboardguard.candidate.repository.CandidateRepository;
import com.onboardguard.candidate.service.impl.CandidateDocumentServiceImpl;
import com.onboardguard.officer.service.DocumentVerificationService;
import com.onboardguard.shared.common.events.DocumentRejectedEvent;
import com.onboardguard.shared.common.events.DocumentVerificationCompletedEvent;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import com.onboardguard.shared.storage.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final CandidateDocumentServiceImpl candidateDocumentService;
    private final CloudStorageService cloudStorageService;
    private final CandidateMapper candidateMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Officer pulls all documents for a specific candidate to review them side-by-side.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getCandidateDocumentsForReview(Long candidateId) {
        return documentRepository.findByCandidateId(candidateId)
                .stream()
                .map(candidateDocumentService::mapToResponseWithUrl)
                .toList();
    }

    /**
     * Officer approves a specific document.
     */
    @Override
    @Transactional
    public void approveDocument(Long documentId, Long officerId) {
        CandidateDocument document = getDocumentById(documentId);

        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new BadRequestException("Document is already verified.");
        }

        document.setStatus(DocumentStatus.VERIFIED);
        document.setVerifiedBy(officerId);
        document.setVerifiedAt(Instant.now());
        document.setRejectionReason(null);

        documentRepository.save(document);
        log.info("Document ID {} VERIFIED by Officer ID {}", documentId, officerId);

        // Optional: Check if ALL documents for this candidate are now verified.
        // If yes, trigger the Screening Engine!
        checkAndAdvanceCandidateStatus(document.getCandidate().getId());
    }

    /**
     * Officer rejects a document. This locks the candidate's screening process
     * and fires an event to email the candidate for re-upload.
     */
    @Override
    @Transactional
    public void rejectDocument(Long documentId, String reason, Long officerId) {
        CandidateDocument document = getDocumentById(documentId);

        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new BadRequestException("Cannot reject a document that has already been verified.");
        }

        document.setStatus(DocumentStatus.REJECTED);
        document.setRejectionReason(reason);
        document.setVerifiedBy(officerId);
        document.setVerifiedAt(Instant.now());

        documentRepository.save(document);

        Candidate candidate = document.getCandidate();

        // Change candidate status so they know action is required
        candidate.setOnboardingStatus(OnboardingStatus.DOCUMENTS_REJECTED);
        candidateRepository.save(candidate);

        log.info("Document ID {} REJECTED by Officer ID {}. Reason: {}", documentId, officerId, reason);

        // Fire the event! Your notification service will listen to this and send an email.
        DocumentRejectedEvent event = new DocumentRejectedEvent(
                candidate.getUser().getEmail(), // Assuming Candidate has a mapping to User
                candidate.getPersonalDetail().getFirstName() + " " + candidate.getPersonalDetail().getLastName(),
                document.getCandidateDocumentType().name(),
                reason
        );
        eventPublisher.publishEvent(event);
    }

    // ══════════════════════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ══════════════════════════════════════════════════════════════

    private CandidateDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
    }

    private void checkAndAdvanceCandidateStatus(Long candidateId) {
        List<CandidateDocument> allDocs = documentRepository.findByCandidateId(candidateId);

        boolean allVerified = allDocs.stream().allMatch(doc -> doc.getStatus() == DocumentStatus.VERIFIED);

        if (allVerified) {
            Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
            candidate.setOnboardingStatus(OnboardingStatus.SCREENING_PENDING);
            candidateRepository.save(candidate);
            log.info("Candidate ID {} has all documents verified. Ready for Screening Engine.", candidateId);

            eventPublisher.publishEvent(new DocumentVerificationCompletedEvent(candidateId));

        }
    }
}