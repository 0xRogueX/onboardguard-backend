package com.onboardguard.screening.controller;

import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.dto.ScreeningResultDto;
import com.onboardguard.screening.entity.ScreeningResult;
import com.onboardguard.screening.mapper.ScreeningMapper;
import com.onboardguard.screening.repository.ScreeningMatchRepository;
import com.onboardguard.screening.repository.ScreeningResultRepository;
import com.onboardguard.screening.service.ScreeningOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screening")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningOrchestrationService orchestrationService;
    private final ScreeningResultRepository     screeningResultRepository;
    private final ScreeningMatchRepository      screeningMatchRepository;
    private final ScreeningMapper               screeningMapper;

    /**
     * Admin manually re-screens a candidate.
     * Normal screenings fire automatically after form submission - not via this endpoint.
     * Returns a summary DTO (no match list — use /results/{id}/matches for detail).
     */
    @PostMapping("/candidates/{candidateId}/re-screen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningResultDto> reScreen(@PathVariable Long candidateId) {
        ScreeningResultDto result = orchestrationService.runScreening(candidateId);
        return ResponseEntity.ok(result);
    }

    /**
     * Full screening history for a candidate — summary DTOs, no match detail.
     * Prevents N+1: matches lazy collection is NOT loaded for list responses.
     */
    @GetMapping("/candidates/{candidateId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<ScreeningResultDto>> getHistory(@PathVariable Long candidateId) {
        List<ScreeningResult> results =
                screeningResultRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
        return ResponseEntity.ok(screeningMapper.toScreeningResultDtoSummaryList(results));
    }

    /**
     * Full match breakdown for one specific screening result.
     * Used in the officer's case detail view — shows which fields matched,
     * similarity scores, corroboration level, and score contributions.
     */
    @GetMapping("/results/{resultId}/matches")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<MatchDetailDto>> getMatchDetails(@PathVariable Long resultId) {
        return ResponseEntity.ok(
                screeningMapper.toMatchDetailDtos(
                        screeningMatchRepository.findByScreeningResultId(resultId)));
    }

    /**
     * Latest screening result for a candidate — full DTO including matches.
     * Used in the officer's alert view to show the most recent screening outcome.
     */
    @GetMapping("/candidates/{candidateId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ScreeningResultDto> getLatest(@PathVariable Long candidateId) {
        return screeningResultRepository
                .findTopByCandidateIdOrderByCreatedAtDesc(candidateId)
                .map(screeningMapper::toScreeningResultDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}