package com.onboardguard.candidate.controller;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.enums.DocumentType;
import com.onboardguard.candidate.service.impl.CandidateDocumentServiceImpl;
import com.onboardguard.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates/profile/documents")
@PreAuthorize("hasRole('CANDIDATE')")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentServiceImpl documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {

        DocumentResponseDto uploadedDoc = documentService.uploadDocument(file, documentType);

        return ResponseEntity.ok(ApiResponse.success(
                documentType + " document uploaded successfully", uploadedDoc));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getMyDocuments() {
        List<DocumentResponseDto> documents = documentService.getCandidateDocuments();
        return ResponseEntity.ok(ApiResponse.success("Documents fetched successfully", documents));
    }
}