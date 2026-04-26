package com.onboardguard.candidate.service.impl;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.entity.CandidateDocument;
import com.onboardguard.candidate.enums.DocumentStatus;
import com.onboardguard.candidate.enums.DocumentType;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.candidate.mapper.CandidateMapper;
import com.onboardguard.candidate.repository.CandidateDocumentRepository;
import com.onboardguard.candidate.repository.CandidateRepository;
import com.onboardguard.shared.common.exception.BadRequestException;
import com.onboardguard.shared.common.exception.ResourceNotFoundException;
import com.onboardguard.shared.storage.CloudStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateDocumentServiceImpl {

    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository documentRepository;
    private final CloudStorageService cloudStorageService;
    private final CandidateMapper candidateMapper;
    private final com.onboardguard.shared.security.SecurityUtils securityUtils;

    @Transactional
    public DocumentResponseDto uploadDocument(MultipartFile file, DocumentType documentType) {
        Long userId = securityUtils.getCurrentUserPrincipal().getUserId();
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found."));

        if (candidate.getOnboardingStatus() == OnboardingStatus.APPROVED ||
                candidate.getOnboardingStatus() == OnboardingStatus.SCREENING_CLEARED) {
            throw new BadRequestException("Onboarding is completed. No further documents can be uploaded.");
        }

        Optional<CandidateDocument> existingDocOpt = documentRepository.findByCandidateIdAndDocumentType(candidate.getId(), documentType);
        CandidateDocument document;

        if (existingDocOpt.isPresent()) {
            document = existingDocOpt.get();

            if (document.getStatus() != DocumentStatus.REJECTED) {
                throw new BadRequestException("Document of type " + documentType + " already exists and is currently " + document.getStatus());
            }

            if (cloudStorageService.exists(document.getCloudStorageKey())) {
                cloudStorageService.delete(document.getCloudStorageKey());
            }

            document.setStatus(DocumentStatus.PENDING);
            document.setRejectionReason(null);
            document.setVerifiedBy(null);
            document.setVerifiedAt(null);
            document.setUploadedAt(Instant.now());
        } else {
            if (candidate.getFormSubmittedAt() != null) {
                throw new BadRequestException("Profile is submitted. You can only re-upload rejected documents.");
            }
            document = candidateMapper.initDocument(candidate, documentType, Instant.now());
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExtension = (extension != null && !extension.isBlank()) ? "." + extension : "";
        String storageKey = String.format("candidates/%d/%s/%s%s",
                candidate.getId(),
                documentType.name(),
                UUID.randomUUID().toString(),
                safeExtension);

        String actualKey = cloudStorageService.upload(storageKey, file);

        document.setCloudStorageKey(actualKey);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setMimeType(file.getContentType());
        document.setFileSizeBytes(file.getSize());

        document = documentRepository.save(document);

        if (candidate.getOnboardingStatus() == OnboardingStatus.PROFESSIONAL_SAVED) {
            candidate.setOnboardingStatus(OnboardingStatus.DOCUMENTS_UPLOADED);
            candidateRepository.save(candidate);
        }

        return mapToResponseWithUrl(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getCandidateDocuments() {
        Long userId = securityUtils.getCurrentUserPrincipal().getUserId();
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found."));

        return documentRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::mapToResponseWithUrl)
                .toList();
    }

    public DocumentResponseDto mapToResponseWithUrl(CandidateDocument document) {
        String presignedUrl = cloudStorageService.generatePresignedUrl(
                document.getCloudStorageKey(),
                Duration.ofMinutes(15)
        );
        return candidateMapper.toDocumentDto(document, presignedUrl);
    }
}