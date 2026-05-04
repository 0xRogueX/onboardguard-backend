package com.onboardguard.screening.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameMatchingUtilTest {

    private final NameMatchingUtil util = new NameMatchingUtil();

    @Test
    void normalizeShouldLowercaseTrimAndRemovePunctuation() {
        assertEquals("r s sharma 123", util.normalize("  R.S.   Sharma!! 123  "));
    }

    @Test
    void isExactMatchShouldIgnoreCaseAndExtraPunctuation() {
        assertTrue(util.isExactMatch("Rohit Sharma", "  rohit.sharma  "));
    }

    @Test
    void advancedNameMatchShouldDetectExactEquivalentValues() {
        NameMatchingUtil.NameMatchResult result = util.advancedNameMatch(
                "Rohit Sharma",
                "  ROHIT   SHARMA ",
                0.90
        );

        assertAll(
                () -> assertTrue(result.matched()),
                () -> assertTrue(result.exact()),
                () -> assertEquals(1.0, result.similarity(), 0.0001)
        );
    }

    @Test
    void advancedNameMatchShouldDetectInitialsExpansion() {
        NameMatchingUtil.NameMatchResult result = util.advancedNameMatch(
                "R.S. Sharma",
                "Rohit Suresh Sharma",
                0.90
        );

        assertAll(
                () -> assertTrue(result.matched()),
                () -> assertFalse(result.exact()),
                () -> assertEquals(0.90, result.similarity(), 0.0001)
        );
    }

    @Test
    void advancedNameMatchShouldRejectUnrelatedNames() {
        NameMatchingUtil.NameMatchResult result = util.advancedNameMatch(
                "Priya Mehta",
                "Rohit Sharma",
                0.90
        );

        assertFalse(result.matched());
        assertFalse(result.exact());
        assertEquals(0.0, result.similarity(), 0.0001);
    }
}

