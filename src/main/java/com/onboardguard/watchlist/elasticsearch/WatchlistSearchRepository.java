package com.onboardguard.watchlist.elasticsearch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WatchlistSearchRepository extends JpaRepository<WatchlistDocument , String> {
    // Elasticsearch generates the DSL query automatically from this method name
    List<WatchlistDocument> findByPrimaryNameOrAliasesAndIsActiveTrue(String name, String alias);
}
