package com.onboardguard.screening.dto;

import lombok.Builder;

/**
 * Flat snapshot of a candidate's data passed to the screening engine.
 * Using a dedicated DTO means strategies never hold a reference to the
 * Candidate JPA entity, keeping them stateless and testable.
 */
@Builder
public record CandidateScreeningData (
        Long   candidateId,

        // Personal
        String fullName,           // e.g. "Rohit S. Sharma"
        String fullNameNormalized, // lowercase, trimmed, spaces-collapsed
        String panNumber,          // uppercase, no spaces
        String aadhaarNumber,      // digits only
        String passportNumber,

        // Professional
        String organizationName,
        String organizationNameNormalized,
        String designation,
        String designationNormalized,

        // Type (Employee / Vendor / Contractor) — for future rule extensions
        String candidateType
) { }