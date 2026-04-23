package com.onboardguard.watchlist.service;

import com.onboardguard.shared.common.events.WatchlistEntryCreatedEvent;
import com.onboardguard.shared.common.events.WatchlistEntryDeactivatedEvent;
import com.onboardguard.shared.common.events.WatchlistEntryUpdatedEvent;
import com.onboardguard.watchlist.dto.*;
import com.onboardguard.watchlist.elasticsearch.WatchlistDocument;
import com.onboardguard.watchlist.elasticsearch.WatchlistSearchRepository;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import com.onboardguard.watchlist.repository.WatchlistAliasRepository;
import com.onboardguard.watchlist.repository.WatchlistEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistSyncService {

    private final WatchlistEntryRepository entryRepository;
    private final WatchlistAliasRepository aliasRepository;
    private final WatchlistSearchRepository esRepository;

    /**
     * EVENT LISTENER: This method ONLY executes if the PostgreSQL transaction
     * successfully commits. It runs asynchronously to prevent blocking the HTTP response.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEntryCreatedOrUpdated(WatchlistEntryCreatedEvent event) {
        log.info("Syncing Entry ID {} to Elasticsearch", event.entryId());
        syncToElasticsearch(event.entryId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEntryUpdated(WatchlistEntryUpdatedEvent event) {
        syncToElasticsearch(event.entryId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEntryDeactivated(WatchlistEntryDeactivatedEvent event) {
        // We fetch and update the ES doc to isActive=false, so it drops out of search results
        syncToElasticsearch(event.entryId());
    }

    private void syncToElasticsearch(Long entryId) {
        WatchlistEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found in DB"));

        List<String> aliases = aliasRepository.findByEntryId(entryId).stream()
                .map(alias -> alias.getAliasNameNormalized())
                .collect(Collectors.toList());

        WatchlistDocument document = WatchlistDocument.builder()
                .id(entry.getId().toString())
                .primaryName(entry.getPrimaryNameNormalized())
                .aliases(aliases)
                .categoryCode(entry.getCategory().getCategoryCode())
                .severity(String.valueOf(entry.getSeverity()))
                .organizationName(entry.getOrganizationName())
                .isActive(entry.getIsActive())
                .build();

        esRepository.save(document);
        log.info("Successfully indexed Watchlist Document {} into Elasticsearch", document.getId());
    }
}