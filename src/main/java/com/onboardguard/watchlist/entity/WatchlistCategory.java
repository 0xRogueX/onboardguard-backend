package com.onboardguard.watchlist.entity;

import com.onboardguard.shared.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "watchlist_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistCategory extends BaseEntity {

    @Column(name = "category_code", unique = true, nullable = false)
    private String categoryCode; // e.g., FRAUD_REGISTRY, PEP_LIST

    private String categoryName;
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matching_fields", columnDefinition = "jsonb")
    private Map<String, Object> matchingFields;

    private Double baseScoreMultiplier;
    private Boolean isActive;
    private Integer displayOrder;
}