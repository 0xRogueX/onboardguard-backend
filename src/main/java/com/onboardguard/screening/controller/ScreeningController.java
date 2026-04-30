package com.onboardguard.screening.controller;

import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.service.ScreeningOrchestrationService;
import com.onboardguard.screening.service.ScreeningQueryService;
import com.onboardguard.shared.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screening")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningOrchestrationService orchestrationService;
    private final ScreeningQueryService queryService;

    // Manually trigger a re-screen for a candidate.
    @PostMapping("/candidates/{candidateId}/re-screen")
    public ResponseEntity<ApiResponse<ScreeningResultDto>> reScreen(@PathVariable Long candidateId) {
        ScreeningResultDto result = orchestrationService.runScreening(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Screening completed successfully", result));
    }

    // Full screening history for a candidate (Summaries only).
    @GetMapping("/candidates/{candidateId}/results")
    public ResponseEntity<ApiResponse<List<ScreeningResultDto>>> getHistory(@PathVariable Long candidateId) {
        List<ScreeningResultDto> history = queryService.getHistory(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Screening history retrieved", history));
    }

    // Full match breakdown for a specific screening result.
    @GetMapping("/results/{resultId}/matches")
    public ResponseEntity<ApiResponse<List<MatchDetailDto>>> getMatchDetails(@PathVariable Long resultId) {
        List<MatchDetailDto> matches = queryService.getMatchDetails(resultId);
        return ResponseEntity.ok(ApiResponse.success("Match details retrieved", matches));
    }

    // The latest screening result for a candidate (Full details + Matches).
    @GetMapping("/candidates/{candidateId}/latest")
    public ResponseEntity<ApiResponse<ScreeningResultDto>> getLatest(@PathVariable Long candidateId) {
        ScreeningResultDto latestResult = queryService.getLatest(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Latest screening result retrieved", latestResult));
    }
}