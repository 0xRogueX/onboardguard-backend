package com.onboardguard.officer.service;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentVerificationService {
    List<DocumentResponseDto> getCandidateDocumentsForReview(Long candidateId);

    void approveDocument(Long documentId, Long officerId);

    void rejectDocument(Long documentId, String reason, Long officerId);
}
