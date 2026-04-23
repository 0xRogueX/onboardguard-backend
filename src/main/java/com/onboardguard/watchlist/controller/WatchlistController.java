package com.onboardguard.watchlist.controller;

import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.watchlist.dto.CandidateMatchRequestDto;
import com.onboardguard.watchlist.dto.WatchlistCategoryDto;
import com.onboardguard.watchlist.dto.WatchlistEntryRequestDto;
import com.onboardguard.watchlist.dto.WatchlistEntryResponseDto;
import com.onboardguard.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    /**
     * 1. THE MATCHING ENGINE TRIGGER
     * Takes candidate data and runs it through Elasticsearch to find fuzzy matches.
     * Uses POST because the candidate payload can be large and contains PII.
     */
    @PostMapping("/match")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER_L1' ,'ROLE_OFFICER_L2', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<WatchlistEntryResponseDto>>> matchCandidate(
            @RequestBody @Valid CandidateMatchRequestDto request) {

        List<WatchlistEntryResponseDto> matches = watchlistService.findMatches(request);
        return ResponseEntity.ok(ApiResponse.success("Matching completed", matches));
    }

    /**
     * 2. GET ALL ACTIVE ENTRIES (For Admin Dashboard Data Grid)
     * Supports pagination, sorting, and basic filtering via Pageable.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_OFFICER_L1' ,'ROLE_OFFICER_L2', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<WatchlistEntryResponseDto>>> getAllActiveEntries(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success("Getting all active entries", watchlistService.getAllActiveEntries(pageable)));
    }

    /**
     * 3. GET ENTRY DETAILS
     * Fetches the full profile (including aliases and evidence) of a specific flagged entry.
     * Used when an Officer clicks on a match to investigate further.
     */
    @GetMapping("/{entryId}")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER_L1' ,'ROLE_OFFICER_L2', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<WatchlistEntryResponseDto>> getEntryDetails(@PathVariable Long entryId) {

        return ResponseEntity.ok(ApiResponse.success("Getting entry if entry ID : " + entryId, watchlistService.getEntryDetails(entryId)));

    }

    /**
     * 4. GET CATEGORIES
     * Used to populate dropdown filters on the frontend UI.
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER_L1' ,'ROLE_OFFICER_L2', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<WatchlistCategoryDto>>> getCategories() {

        return ResponseEntity.ok(ApiResponse.success("Getting active categories", watchlistService.getActiveCategories()));
    }


//    @PostMapping("/request")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    public ResponseEntity<String> requestCreateEntry(@RequestBody WatchlistEntryRequestDto requestDto) throws Exception {
//        // Extract Maker ID from Spring Security
//        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
//        requestDto.setRequestedBy(currentUser);
//
//        watchlistService.requestCreateEntry(requestDto);
//        return ResponseEntity.accepted().body("Watchlist entry creation requested. Pending Checker  approval.");
//    }
//
//    @PostMapping("/approve/{pendingId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<String> approveEntry(@PathVariable Long pendingId) throws Exception {
//        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
//        watchlistService.approveWatchlistEntry(pendingId, currentUser);
//
//        return ResponseEntity.ok("Watchlist entry approved and synchronized to search cluster.");
//    }
//
//    @PostMapping("/{entryId}/aliases")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    public ResponseEntity<String> addAlias(@PathVariable Long entryId, @RequestParam String aliasName, @RequestParam String type) {
//        watchlistService.addAlias(entryId, aliasName, type);
//        return ResponseEntity.ok("Alias added successfully.");
//    }


}