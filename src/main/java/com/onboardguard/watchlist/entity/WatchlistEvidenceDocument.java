package com.onboardguard.watchlist.entity;

import com.onboardguard.shared.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_evidence_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistEvidenceDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private WatchlistEntry entry;

    @Column(nullable = false)
    private String cloudStorageKey; // S3 Key

    private String documentTitle;
    private String documentType; // PDF, JPG
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}