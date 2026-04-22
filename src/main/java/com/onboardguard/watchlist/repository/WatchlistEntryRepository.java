package com.onboardguard.watchlist.repository;

import com.onboardguard.watchlist.entity.WatchlistEntry;
import org.hibernate.sql.exec.spi.JdbcCallParameterRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry , Long> {

    // Validates if entry is active AND today's date falls between effectiveFrom and effectiveTo
    @Query("SELECT e FROM WatchlistEntry e WHERE e.isActive = true " +
            "AND (e.effectiveFrom IS NULL OR e.effectiveFrom  <= CURRENT_DATE )" +
            "AND (e.effectiveTo IS NULL OR e.effectiveTo >= CURRENT_DATE )")
    List<WatchlistEntry> findAllActiveAndEffective();
}
