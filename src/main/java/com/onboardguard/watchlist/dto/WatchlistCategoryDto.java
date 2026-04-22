package com.onboardguard.watchlist.dto;

import lombok.Data;

@Data
public class WatchlistCategoryDto {
    private String categoryCode;
    private String categoryName;
    private String description;
    private Double baseScoreMultiplier;
    private Integer displayOrder;
}