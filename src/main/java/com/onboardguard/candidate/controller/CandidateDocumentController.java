package com.onboardguard.candidate.controller;

import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.enums.CandidateDocumentType;
import com.onboardguard.candidate.service.CandidateDocumentService;
import com.onboardguard.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates/profile/documents")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateDocumentType") CandidateDocumentType candidateDocumentType) {

        DocumentResponseDto uploadedDoc = documentService.uploadDocument(file, candidateDocumentType);

        return ResponseEntity.ok(ApiResponse.success(
                candidateDocumentType + " document uploaded successfully", uploadedDoc));
    }

    @PostMapping(path = "/re-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponseDto>> reUploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateDocumentType") CandidateDocumentType candidateDocumentType) {

        DocumentResponseDto uploadedDoc = documentService.reUploadDocument(file, candidateDocumentType);

        return ResponseEntity.ok(ApiResponse.success(
                candidateDocumentType + " document re-uploaded successfully", uploadedDoc));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getMyDocuments() {
        List<DocumentResponseDto> documents = documentService.getCandidateDocuments();
        return ResponseEntity.ok(ApiResponse.success("Documents fetched successfully", documents));
    }
}