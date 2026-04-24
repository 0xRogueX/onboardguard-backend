package com.onboardguard.candidate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "candidate_personal_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatePersonalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    private String firstName;
    private String middleName;
    private String lastName;
    private String fullNameNormalized;

    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;

    @Column(unique = true)
    private String panNumber;

    @Column(unique = true)
    private String adhaarNumber;

    @Column(unique = true)
    private String passportNumber;

    private String addressLine1;
    private String addressCity;
    private String addressState;
    private String addressPincode;

    @Builder.Default
    private String addressCountry = "INDIA";
}