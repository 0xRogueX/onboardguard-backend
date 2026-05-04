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
    

    // QUEUE DASHBOARD QUERIES (Read-Only)

    // L1 QUEUE -> fresh OPEN cases AND cases currently IN_REVIEW by the requesting officer
    @Query("""
        SELECT c FROM Case c
        WHERE (c.status = :status AND c.assignedOfficerId IS NULL)
        OR (c.status = :inReviewStatus AND c.assignedOfficerId = :officerId)
        ORDER BY c.createdAt ASC
    """)
    List<Case> findAvailableCasesForQueue(@Param("status") CaseStatus status, @Param("inReviewStatus") CaseStatus inReviewStatus, @Param("officerId") Long officerId);

    // L2 QUEUE -> ESCALATED cases that nobody has claimed OR claimed by the requesting L2 officer
    @Query("""
        SELECT c FROM Case c
        WHERE (c.status = :status AND c.assignedOfficerId IS NULL)
        OR (c.status = :status AND c.assignedOfficerId = :officerId)
        ORDER BY c.escalatedAt ASC
    """)
    List<Case> findEscalatedCasesForL2Queue(@Param("status") CaseStatus status, @Param("officerId") Long officerId);


    // CLAIM QUERIES (Pessimistic Locking to prevent double-assignment)

    // MANUAL CLAIM (L1 & L2 By ID)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT c FROM Case c WHERE c.id = :caseId")
    Optional<Case> findByIdForUpdate(@Param("caseId") Long caseId);

    // FIFO CLAIM (L1 - Finds oldest OPEN case)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT c FROM Case c
        WHERE c.status = :status
        AND c.assignedOfficerId IS NULL
        ORDER BY c.createdAt ASC
    """)
    Optional<Case> findFirstNextOpenCaseForUpdate(@Param("status") CaseStatus status);

    // FIFO CLAIM (L2 - Finds oldest ESCALATED case)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT c FROM Case c
        WHERE c.status = :status
        AND c.assignedOfficerId IS NULL
        ORDER BY c.escalatedAt ASC
    """)
    Optional<Case> findFirstNextEscalatedCaseForUpdate(@Param("status") CaseStatus status);

    long countByStatus(CaseStatus status);

    long countByIsSlaBreachedTrue();
}