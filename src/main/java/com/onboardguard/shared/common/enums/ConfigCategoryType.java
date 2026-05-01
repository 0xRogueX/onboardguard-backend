package com.onboardguard.shared.common.enums;

public enum ConfigCategoryType{
    SCREENING,  // For risk scores, thresholds, multipliers
    STORAGE,    // For AWS/Cloudinary, TTLs, file sizes
    BUSINESS,   // For SLAs, auto-rejection rules
    SYSTEM      // For general app configs (future use)
}