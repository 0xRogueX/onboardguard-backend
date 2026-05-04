package com.onboardguard.watchlist.repository;

import com.onboardguard.watchlist.entity.WatchlistEvidenceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistEvidenceDocRepository extends JpaRepository<WatchlistEvidenceDocument , Long> {

    List<WatchlistEvidenceDocument> findByEntryId(Long entryId);

    @Query("""
        SELECT d FROM WatchlistEvidenceDocument d
        JOIN FETCH d.entry
        JOIN FETCH d.source
    """)
    List<WatchlistEvidenceDocument> findAllWithRelations();
}
