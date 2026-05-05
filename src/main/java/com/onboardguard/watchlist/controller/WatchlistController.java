package com.onboardguard.watchlist.controller;

import com.onboardguard.shared.common.dto.ApiResponse;
import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.watchlist.dto.WatchlistCategoryDto;
import com.onboardguard.watchlist.dto.WatchlistEntryResponseDto;
import com.onboardguard.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WatchlistEntryResponseDto>>> getAllActiveEntries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CategoryCode category,
            @RequestParam(required = false) SeverityLevel severity,
            Pageable pageable) {
        Page<WatchlistEntryResponseDto> pageData = watchlistService.getAllActiveEntries(search, category, severity, pageable);
        return ResponseEntity.ok(ApiResponse.success("Fetched watchlist entries", pageData));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<WatchlistEntryResponseDto>>> manualSearch(
            @RequestParam String name) {

        List<WatchlistEntryResponseDto> results = watchlistService.searchRawDictionary(name);
        return ResponseEntity.ok(ApiResponse.success("Search complete", results));
    }


     // Fetches the full profile (including aliases and evidence documents) of a specific flagged entry.
    @GetMapping("/{entryId}")
    public ResponseEntity<ApiResponse<WatchlistEntryResponseDto>> getEntryDetails(@PathVariable Long entryId) {
        WatchlistEntryResponseDto details = watchlistService.getEntryDetails(entryId);
        return ResponseEntity.ok(ApiResponse.success("Fetched entry details", details));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<WatchlistCategoryDto>>> getCategories() {
        List<WatchlistCategoryDto> categories = watchlistService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Fetched categories", categories));
    }

}