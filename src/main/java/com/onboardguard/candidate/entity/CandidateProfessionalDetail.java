package com.onboardguard.candidate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "candidate_professional_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfessionalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    private String currentOrganization;
    private String cinNumber;
    private String dinNumber;
    private String currentDesignation;

    @Column(precision = 4, scale = 2)
    private BigDecimal totalExperienceYears;

    private String previousOrganization;
    private String previousDesignation;

    private String vendorCompanyName;
    private String vendorGstNumber;

    private String highestQualification;
    private String universityName;
    private Integer graduationYear;
}