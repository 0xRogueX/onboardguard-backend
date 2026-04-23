package com.onboardguard.watchlist.mapper;

import com.onboardguard.watchlist.dto.WatchlistEntryResponseDto;
import com.onboardguard.watchlist.entity.WatchlistAlias;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.entity.WatchlistEvidenceDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring" tells MapStruct to generate a standard Spring @Component,
// so you can use @RequiredArgsConstructor to inject it into your services.
@Mapper(componentModel = "spring")
public interface WatchlistMapper {

    /**
     * Maps the core WatchlistEntry entity to the Response DTO.
     * We explicitly tell MapStruct how to flatten the nested Category entity.
     */
    @Mapping(source = "category.categoryCode", target = "categoryCode")
    @Mapping(source = "category.categoryName", target = "categoryName")
    // aliases and evidenceDocuments are ignored here because they are not lists inside
    // the WatchlistEntry entity. They are fetched separately by the Service.
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "evidenceDocuments", ignore = true)
    WatchlistEntryResponseDto toResponseDto(WatchlistEntry entry);

    /**
     * Maps the Alias entity to the nested AliasDto
     */
    WatchlistEntryResponseDto.AliasDto toAliasDto(WatchlistAlias alias);

    /**
     * Maps the Evidence entity to the nested EvidenceDto
     */
    WatchlistEntryResponseDto.EvidenceDto toEvidenceDto(WatchlistEvidenceDocument document);
}