package com.onboardguard.watchlist.service.impl;

import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.shared.common.enums.FileFormat;
import com.onboardguard.shared.config.service.SystemConfigService;
import com.onboardguard.shared.storage.CloudStorageService;
import com.onboardguard.watchlist.dto.WatchlistCategoryDto;
import com.onboardguard.watchlist.dto.WatchlistEntryResponseDto;
import com.onboardguard.watchlist.elasticsearch.WatchlistDocument;
import com.onboardguard.watchlist.entity.WatchlistCategory;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.mapper.WatchlistMapper;
import com.onboardguard.watchlist.repository.*;
import com.onboardguard.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Crucial: Disables Hibernate dirty checking for massive speed boosts
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistEntryRepository entryRepository;
    private final WatchlistCategoryRepository categoryRepository;
    private final WatchlistAliasRepository aliasRepository;
    private final WatchlistEvidenceDocRepository evidenceRepository;

    private final CloudStorageService cloudStorageService;
    private final SystemConfigService systemConfigService;

    private final ElasticsearchOperations elasticsearchOperations;
    private final WatchlistMapper watchlistMapper;

    @Override
    public Page<WatchlistEntryResponseDto> getAllActiveEntries(String query, CategoryCode categoryCode, SeverityLevel severity, Pageable pageable) {
        log.debug("Fetching paginated watchlist entries with filters: query={}, category={}, severity={}", query, categoryCode, severity);

        String search = (query != null && !query.trim().isEmpty()) ? "%" + query.trim().toLowerCase() + "%" : null;

        return entryRepository.findWithFilters(search, categoryCode, severity, pageable)
                .map(watchlistMapper::toResponseDto);
    }


    private String fileFormateToMine(FileFormat fileFormatEnum) {
        if (fileFormatEnum == null) {
            return "application/pdf"; // Safe default based on allowed types
        }
        return fileFormatToMimeType(fileFormatEnum.name());
    }


    private String fileFormatToMimeType(String fileFormat) {
        if (fileFormat == null) {
            return "application/pdf"; // Safe default
        }

        String f = fileFormat.trim().toLowerCase();

        // If it already looks like a mime type, return as-is
        if (f.contains("/")) {
            return f;
        }

        // Strip a leading dot if present
        if (f.startsWith(".")) {
            f = f.substring(1);
        }

        return switch (f) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            default -> {
                log.warn("Unsupported file format '{}'. Defaulting to application/pdf to satisfy Cloudinary constraints.", fileFormat);
                yield "application/pdf"; // Fallback to an allowed type so it doesn't crash the presigned URL generator
            }
        };
    }

    @Override
    public WatchlistEntryResponseDto getEntryDetails(Long entryId) {
        log.debug("Fetching details for Watchlist Entry ID: {}", entryId);
        WatchlistEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist entry not found"));

        WatchlistEntryResponseDto response = watchlistMapper.toResponseDto(entry);

        // Append related nested data
        response.setAliases(aliasRepository.findByEntryId(entryId).stream()
                .map(watchlistMapper::toAliasDto).collect(Collectors.toList()));

        int presignMinutes = systemConfigService.getInt("STORAGE_PRESIGNED_URL_TTL_MINUTES", 15);

        response.setEvidenceDocuments(
                evidenceRepository.findByEntryId(entryId).stream()
                        .map(document -> {
                            WatchlistEntryResponseDto.EvidenceDto dto = watchlistMapper.toEvidenceDto(document);
                            String mimeType = fileFormateToMine(document.getFileFormat());
                            String presignedUrl =  cloudStorageService.generatePresignedUrl(
                                    document.getCloudStorageKey(),
                                    Duration.ofMinutes(presignMinutes),
                                    mimeType
                            );
                            dto.setCloudStorageKey(presignedUrl);
                            return dto;
                        }).collect(Collectors.toList())
        );

        return response;
    }

    @Override
    public List<WatchlistCategoryDto> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getIsActive() == null || c.getIsActive())
                .map(watchlistMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WatchlistEntryResponseDto> findExactIdMatch(String panNumber, String aadhaarNumber, String cin, String din) {
        log.info("Searching DB for exact PAN/Aadhaar/cin/din match...");

        if (panNumber == null && aadhaarNumber == null) {
            return List.of();
        }

        List<WatchlistEntry> exactMatches = entryRepository.findByPanNumberOrAadhaarNumberOrDinNumberOrCinNumber(panNumber, aadhaarNumber , cin ,din);

        return exactMatches.stream()
                .map(watchlistMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WatchlistEntryResponseDto> searchRawDictionary(String searchName) {
        log.info("Elasticsearch disabled. Falling back to basic DB search for: {}", searchName);

        // Fallback: Just return exact/partial matches from Postgres instead of fuzzy matching
        // (You might need to add this method to your WatchlistEntryRepository)
        return List.of();
    }
//        log.info("Executing fuzzy Elasticsearch match for: {}", searchName);
//
//        NativeQuery fuzzyQuery = NativeQuery.builder()
//                .withQuery(q -> q
//                        .multiMatch(m -> m
//                                .fields("primaryName", "aliases")
//                                .query(searchName)
//                                .fuzziness("AUTO") // Automatically handles character typos
//                        )
//                )
//                .build();
//
//        SearchHits<WatchlistDocument> esHits = elasticsearchOperations.search(fuzzyQuery, WatchlistDocument.class);
//
//        // Extract internal Postgres IDs from the Elasticsearch results
//        List<Long> matchedIds = esHits.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .filter(WatchlistDocument::getIsActive) // Only return active, verified entries
//                .map(doc -> Long.valueOf(doc.getId()))
//                .collect(Collectors.toList());
//
//        if (matchedIds.isEmpty()) {
//            return List.of();
//        }
//
//        // Fetch full rich data from Postgres using the matched IDs
//        List<WatchlistEntry> fullEntries = entryRepository.findAllById(matchedIds);
//        return fullEntries.stream()
//                .map(watchlistMapper::toResponseDto)
//                .collect(Collectors.toList());
//    }
}