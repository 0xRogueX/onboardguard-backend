package com.onboardguard.officer.repository;

import com.onboardguard.officer.entity.Case;
import com.onboardguard.shared.common.enums.CaseStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    @Modifying
    @Query("""
        UPDATE Case c
        SET c.isSlaBreached = true
        WHERE c.isSlaBreached = false
        AND c.status != :resolvedStatus
        AND c.slaDueDate < :now
    """)
    int markBreachedCases(@Param("resolvedStatus") CaseStatus resolvedStatus,
                          @Param("now") Instant now);

    // L1 QUEUE -> only fresh cases
    @Query("""
        SELECT c FROM Case c
        WHERE c.status = :status
        AND c.assignedOfficerId IS NULL
        ORDER BY c.createdAt ASC
    """)
    List<Case> findAvailableCasesForQueue(@Param("status") CaseStatus status);

    // L2 QUEUE -> only escalated
    @Query("""
        SELECT c FROM Case c
        WHERE c.status = :status
        AND c.assignedOfficerId IS NULL
        ORDER BY c.escalatedAt ASC
    """)
    List<Case> findEscalatedCasesForL2Queue(@Param("status") CaseStatus status);

    // LOCKED CLAIM
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT c FROM Case c
        WHERE c.id = :caseId
    """)
    Optional<Case> findByIdForUpdate(@Param("caseId") Long caseId);

    // FIFO CLAIM (L2)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT c FROM Case c
        WHERE c.status = :status
        AND c.assignedOfficerId IS NULL
        ORDER BY c.escalatedAt ASC
    """)
    Optional<Case> findNextEscalatedCaseForUpdate(@Param("status") CaseStatus status);
}
