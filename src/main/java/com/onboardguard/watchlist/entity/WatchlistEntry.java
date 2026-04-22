package com.onboardguard.watchlist.entity;

import com.onboardguard.shared.common.entity.BaseEntity;
import com.onboardguard.shared.common.enums.SeverityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "watchlist_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private WatchlistCategory category;

    @Column(nullable = false)
    private String primaryName;

    @Column(nullable = false)
    private String primaryNameNormalized;

    @Column(nullable = false)
    private SeverityLevel severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String sourceName;
    private Double sourceCredibilityWeight;

    private String panNumber;
    private String aadhaarNumber;
    private String dinNumber;
    private String cinNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_specific_data", columnDefinition = "jsonb")
    private Map<String, Object> categorySpecificData;

    private String organizationName;
    private String designation;
    private LocalDate dateOfBirth;
    private String nationality;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private Boolean isActive = false; // Default false until Maker-Checker approval

    private String notes;
    private String approvedBy;
    private LocalDateTime approvedAt;
}