package com.onboardguard.officer.repository;

import com.onboardguard.officer.entity.Alert;
import com.onboardguard.shared.common.enums.AlertStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Finds the most urgent OPEN alert based on SLA deadline.
     * @Lock(LockModeType.PESSIMISTIC_WRITE) forces the database to lock this row.
     * If another officer queries at the exact same time, they must wait or grab the NEXT row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Alert> findFirstByStatusOrderBySlaDeadlineAsc(AlertStatus status);

    /**
     * Bulk updates all active alerts where the SLA deadline has passed.
     * Returns the integer count of exactly how many rows were updated.
     */
    @Modifying
    @Query("UPDATE Alert a SET a.isSlaBreached = true WHERE a.isSlaBreached = false AND a.status IN :activeStatuses AND a.slaDeadline < :now")
    int markBreachedAlerts(@Param("activeStatuses") List<AlertStatus> activeStatuses, @Param("now") Instant now);
}
