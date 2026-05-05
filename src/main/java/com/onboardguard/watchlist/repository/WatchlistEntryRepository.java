package com.onboardguard.watchlist.repository;

import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.shared.common.enums.SeverityLevel;
import com.onboardguard.watchlist.entity.WatchlistEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry , Long> {

    @Query("SELECT e FROM WatchlistEntry e WHERE e.isActive = true " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom  <= CURRENT_DATE )" +
            "AND (e.effectiveTo IS NULL OR e.effectiveTo >= CURRENT_DATE )")
    Page<WatchlistEntry> findAllActiveAndEffective(Pageable pageable);

    @Query(value = "SELECT e FROM WatchlistEntry e WHERE e.isActive = true " +
            "AND (:category IS NULL OR e.category.code = :category) " +
            "AND (:severity IS NULL OR e.severity = :severity) " +
            "AND (:query IS NULL OR LOWER(e.primaryName) LIKE :query OR LOWER(e.organizationName) LIKE :query) " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom <= CURRENT_DATE) " +
            "AND (e.effectiveTo IS NULL OR e.effectiveTo >= CURRENT_DATE)",
            countQuery = "SELECT COUNT(e) FROM WatchlistEntry e WHERE e.isActive = true " +
                    "AND (:category IS NULL OR e.category.code = :category) " +
                    "AND (:severity IS NULL OR e.severity = :severity) " +
                    "AND (:query IS NULL OR LOWER(e.primaryName) LIKE :query OR LOWER(e.organizationName) LIKE :query) " +
                    "AND (e.effectiveFrom IS NULL OR e.effectiveFrom <= CURRENT_DATE) " +
                    "AND (e.effectiveTo IS NULL OR e.effectiveTo >= CURRENT_DATE)")
    Page<WatchlistEntry> findWithFilters(
            @Param("query") String query,
            @Param("category") CategoryCode category,
            @Param("severity") SeverityLevel severity,
            Pageable pageable);

    @Query("SELECT e FROM WatchlistEntry e WHERE e.isActive = true " +
            "AND e.category.code = :categoryCode " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom  <= CURRENT_DATE )" +
            "AND (e.effectiveTo IS NULL OR e.effectiveTo >= CURRENT_DATE )")
    Page<WatchlistEntry> findAllActiveAndEffectiveByCategory(@Param("categoryCode") CategoryCode categoryCode, Pageable pageable);

    Page<WatchlistEntry> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT e FROM WatchlistEntry e WHERE e.isActive = true " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom <= CURRENT_DATE) " +
            "AND (e.effectiveTo   IS NULL OR e.effectiveTo   >= CURRENT_DATE) " +
            "AND ((:pan     IS NOT NULL AND e.panNumber     = :pan)     OR " +
            "     (:aadhaar IS NOT NULL AND e.aadhaarNumber = :aadhaar) OR " +
            "     (:din     IS NOT NULL AND e.dinNumber     = :din)     OR " +
            "     (:cin     IS NOT NULL AND e.cinNumber     = :cin))")
    List<WatchlistEntry> findActiveExactIdMatch(
            @Param("pan")     String pan,
            @Param("aadhaar") String aadhaar,
            @Param("din")     String din,
            @Param("cin")     String cin);

    /**
     * Used by BasicScreeningStrategy.
     *
     * Returns all active watchlist entries whose effective window covers the
     * given date. No alias JOIN — basic strategy never checks aliases.
     */
    @Query("SELECT e FROM WatchlistEntry e " +
            "WHERE e.isActive = true " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom <= :screeningDate) " +
            "AND (e.effectiveTo   IS NULL OR e.effectiveTo   >= :screeningDate)")
    List<WatchlistEntry> findAllActiveOnDate(@Param("screeningDate") LocalDate date);

    /**
     * Used by AdvancedScreeningStrategy.
     *
     * Same active+effective filter as above, but JOIN FETCHes aliases in one
     * query so the strategy can iterate entry.getAliases() without triggering
     * N+1 lazy loads.
     *
     * DISTINCT is required because a LEFT JOIN FETCH on a collection produces
     * duplicate parent rows in JPQL — one row per alias per entry.
     */
    @Query("SELECT DISTINCT e FROM WatchlistEntry e " +
            "LEFT JOIN FETCH e.aliases " +
            "WHERE e.isActive = true " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom <= :screeningDate) " +
            "AND (e.effectiveTo   IS NULL OR e.effectiveTo   >= :screeningDate)")
    List<WatchlistEntry> findAllActiveOnDateWithAliases(@Param("screeningDate") LocalDate date);

    List<WatchlistEntry> findByPanNumberOrAadhaarNumberOrDinNumberOrCinNumber(
            String pan, String aadhaar, String din, String cin);
}