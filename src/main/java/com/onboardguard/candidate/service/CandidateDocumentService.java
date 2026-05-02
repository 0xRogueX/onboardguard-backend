package com.onboardguard.candidate.service;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.entity.CandidateDocument;
import com.onboardguard.candidate.enums.CandidateDocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateDocumentService {

    DocumentResponseDto uploadDocument(MultipartFile file, CandidateDocumentType candidateDocumentType);

    DocumentResponseDto reUploadDocument(MultipartFile file, CandidateDocumentType candidateDocumentType);

    List<DocumentResponseDto> getCandidateDocuments();

    DocumentResponseDto mapToResponseWithUrl(CandidateDocument document);
}
