package com.onboardguard.screening.strategy;

import com.onboardguard.screening.dto.CandidateScreeningData;
import com.onboardguard.screening.dto.ScreeningResultDto;

/**
 *  - BasicScreeningStrategy  -> exact matching only
 *  - AdvancedScreeningStrategy -> initials expansion + fuzzy + alias + multi-field corroboration
 */
public interface ScreeningStrategy {

    ScreeningResultDto screen(CandidateScreeningData candidate);

    String strategyName();  // BASIC/ADVANCED
}