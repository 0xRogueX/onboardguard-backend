package com.onboardguard.officer.service;

import com.onboardguard.officer.dto.CaseDetailDto;
import com.onboardguard.officer.dto.EscalateCaseDto;
import com.onboardguard.officer.dto.ResolveCaseDto;

import java.util.List;

public interface CaseService {

    // Queues
    List<CaseDetailDto> getAvailableCasesForQueue();
    List<CaseDetailDto> getEscalatedCasesQueue();

    // Read Data
    CaseDetailDto getCaseDetails(Long caseId);

    // L1 Actions
    void claimOpenCaseManual(Long caseId, Long l1OfficerId);
    CaseDetailDto claimNextOpenCaseFifo(Long l1OfficerId);
    void escalateCase(Long caseId, EscalateCaseDto dto, Long l1OfficerId);

    // L2 Actions
    void claimEscalatedCaseManual(Long caseId, Long l2OfficerId);
    CaseDetailDto claimNextEscalatedCaseFifo(Long l2OfficerId);
    void resolveCase(Long caseId, ResolveCaseDto dto, Long l2OfficerId);
}