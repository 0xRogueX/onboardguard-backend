package com.onboardguard.screening.mapper;

import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.entity.CandidatePersonalDetail;
import com.onboardguard.candidate.entity.CandidateProfessionalDetail;
import com.onboardguard.candidate.enums.CandidateType;
import com.onboardguard.candidate.enums.OnboardingStatus;
import com.onboardguard.screening.dto.CandidateScreeningData;
import com.onboardguard.screening.dto.MatchDetailDto;
import com.onboardguard.screening.entity.ScreeningMatch;
import com.onboardguard.screening.enums.CorroborationLevel;
import com.onboardguard.screening.enums.MatchType;
import com.onboardguard.screening.enums.RiskLevel;
import com.onboardguard.screening.enums.ScreeningStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ScreeningMapperTest {

    private final ScreeningMapper mapper = Mappers.getMapper(ScreeningMapper.class);

    @Test
    void toCandidateScreeningDataShouldNormalizeCandidateFields() {
        Candidate candidate = Candidate.builder()
                .candidateType(CandidateType.EMPLOYEE)
                .onboardingStatus(OnboardingStatus.REGISTERED)
                .personalDetail(CandidatePersonalDetail.builder()
                        .firstName("  Rohit ")
                        .middleName("S.")
                        .lastName(" Sharma  ")
                        .panNumber(" bbbps1234c ")
                        .adhaarNumber("1234-5678-9012")
                        .build())
                .professionalDetail(CandidateProfessionalDetail.builder()
                        .currentOrganization("  Onboard Guard  Pvt Ltd ")
                        .currentDesignation("  Software Engineer ")
                        .totalExperienceYears(new BigDecimal("5.50"))
                        .build())
                .build();

        CandidateScreeningData data = mapper.toCandidateScreeningData(candidate);

        assertAll(
                () -> assertEquals("rohit s. sharma", data.getFullName()),
                () -> assertEquals("rohit s. sharma", data.getFullNameNormalized()),
                () -> assertEquals("BBBPS1234C", data.getPanNumber()),
                () -> assertEquals("123456789012", data.getAadhaarNumber()),
                () -> assertEquals("onboard guard pvt ltd", data.getOrganizationNameNormalized()),
                () -> assertEquals("software engineer", data.getDesignationNormalized()),
                () -> assertEquals("EMPLOYEE", data.getCandidateType())
        );
    }

    @Test
    void toScreeningMatchEntityShouldCopyAuditSnapshotFields() {
        MatchDetailDto dto = MatchDetailDto.builder()
                .watchlistEntryId(10L)
                .watchlistPrimaryName("Osama Bin Laden")
                .watchlistCategory("CRIMINAL")
                .watchlistSeverity("HIGH")
                .watchlistSourceName("UN Security Council")
                .watchlistSourceCredibility(1.0)
                .matchType(MatchType.NAME_EXACT)
                .candidateFieldValue("Osama Bin Laden")
                .watchlistFieldValue("Osama Bin Laden")
                .similarityScore(null)
                .basePoints(40.0)
                .sourceCredibilityWeight(1.0)
                .corroborationMultiplier(1.0)
                .categoryBonus(15.0)
                .scoreContribution(55.0)
                .corroborationLevel(CorroborationLevel.NAME_ONLY)
                .suppressed(false)
                .build();

        ScreeningMatch match = mapper.toScreeningMatchEntity(dto);

        assertAll(
                () -> assertEquals(MatchType.NAME_EXACT, match.getMatchType()),
                () -> assertEquals("osama bin laden", match.getWatchlistEntryPrimaryNameSnapshot()),
                () -> assertEquals("criminal", match.getWatchlistCategorySnapshot()),
                () -> assertEquals("high", match.getWatchlistSeveritySnapshot()),
                () -> assertEquals("un security council", match.getWatchlistSourceNameSnapshot()),
                () -> assertEquals(55.0, match.getScoreContribution(), 0.0001),
                () -> assertEquals(CorroborationLevel.NAME_ONLY, match.getCorroborationLevel())
        );
    }

    @Test
    void riskLevelToStatusShouldMapLowToClearAndHighToFlagged() {
        assertAll(
                () -> assertEquals(ScreeningStatus.CLEAR, mapper.riskLevelToStatus(RiskLevel.LOW)),
                () -> assertEquals(ScreeningStatus.FLAGGED, mapper.riskLevelToStatus(RiskLevel.MEDIUM)),
                () -> assertEquals(ScreeningStatus.FLAGGED, mapper.riskLevelToStatus(RiskLevel.HIGH)),
                () -> assertEquals(ScreeningStatus.PENDING, mapper.riskLevelToStatus(null))
        );
    }
}

