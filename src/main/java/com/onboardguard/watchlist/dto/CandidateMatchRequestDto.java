package com.onboardguard.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CandidateMatchRequestDto {

    @NotBlank(message = "Candidate primary name is required for matching")
    private String candidateName;

    private String panNumber;
    private String aadhaarNumber;

    private LocalDate dateOfBirth;
    private String nationality;

    // Optional: If you want to restrict the search to specific severities or categories
    private String filterCategoryCode;
}