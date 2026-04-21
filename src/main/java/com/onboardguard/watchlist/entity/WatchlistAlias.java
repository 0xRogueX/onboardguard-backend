package com.onboardguard.watchlist.entity;

import com.onboardguard.shared.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "watchlist_aliases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistAlias extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private WatchlistEntry entry;

    @Column(nullable = false)
    private String aliasName;

    @Column(nullable = false)
    private String aliasNameNormalized;

    @Column(nullable = false)
    private String aliasType; // e.g., AKA (Also Known As), FKA (Formerly Known As)
}