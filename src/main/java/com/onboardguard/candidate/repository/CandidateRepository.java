package com.onboardguard.candidate.repository;

import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.enums.OnboardingStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByUserId(Long userId);

    Long countByOnboardingStatus(OnboardingStatus onboardingStatus);

    /**
     * GET NEXT CANDIDATE (FIFO Queue with SKIP LOCKED):
     * Finds the oldest candidate waiting for document verification that is not currently locked.
     * Accepts candidates in FORM_SUBMITTED status (set when candidate clicks "Submit Application").
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT c
        FROM Candidate c
        WHERE c.onboardingStatus IN :statuses
          AND c.verificationLockedBy IS NULL
        ORDER BY c.formSubmittedAt ASC
    """)
    Optional<Candidate> findFirstAvailableForVerification(@Param("statuses") List<OnboardingStatus> statuses);

    /**
     * GET QUEUE VIEW (Grid):
     * Fetches all candidates who have submitted their form and are NOT currently locked by any officer.
     * This is what populates the officer's "Candidate Queue" grid.
     *
     * Includes FORM_SUBMITTED (just submitted) and DOCUMENTS_UNDER_REVIEW with no lock
     * (e.g., officer abandoned without finishing).
     */
    @Query("""
        SELECT c 
        FROM Candidate c 
        WHERE c.onboardingStatus IN :statuses 
          AND c.verificationLockedBy IS NULL 
        ORDER BY c.formSubmittedAt ASC
    """)
    List<Candidate> findAvailableCandidatesForVerification(@Param("statuses") List<OnboardingStatus> statuses);
}