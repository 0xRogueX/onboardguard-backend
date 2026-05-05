package com.onboardguard.screening.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NameMatchingUtil {

    public String normalize(String name) {
        if (name == null) return "";

        // Step 1: lowercase + trim
        String s = name.toLowerCase().trim();

        // Step 2: dots -> spaces
        s = s.replace(".", " ");

        // Step 3: collapse spaces
        s = s.replaceAll("\\s+", " ").trim();

        // Step 4: remove everything except lowercase letters, digits, spaces
        s = s.replaceAll("[^a-z0-9 ]", "");

        return s;
    }

    public boolean isExactMatch(String a, String b) {
        if (a == null || b == null) return false;
        return normalize(a).equals(normalize(b));
    }

    public boolean isFuzzyMatch(String a, String b, double threshold) {
        return jaroWinkler(a, b) >= threshold;
    }

    public NameMatchResult advancedNameMatch(String candidateName,
                                             String watchlistName,
                                             double threshold) {
        if (candidateName == null || watchlistName == null) {
            return NameMatchResult.noMatch();
        }

        String nc = normalize(candidateName);
        String nw = normalize(watchlistName);

        if (nc.isEmpty() || nw.isEmpty()) {
            return NameMatchResult.noMatch();
        }

        // Priority 1: exact
        if (nc.equals(nw)) {
            return NameMatchResult.exactMatch();
        }

        // Priority 2: initials expansion (both directions handled inside)
        if (isInitialsExpansionMatch(nc, nw)) {
            return NameMatchResult.fuzzy(0.90);
        }

        // Priority 3: whole-name Jaro-Winkler
        double sim = jaroWinkler(nc, nw);
        if (sim >= threshold) {
            return NameMatchResult.fuzzy(sim);
        }

        return NameMatchResult.noMatch();
    }

    public double jaroWinkler(String a, String b) {
        if (a == null || b == null) return 0.0;
        String s1 = normalize(a);
        String s2 = normalize(b);
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        double jaroScore = jaro(s1, s2);

        // Winkler prefix bonus - up to 4 matching prefix characters
        int prefixLen = 0;
        int limit = Math.min(4, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < limit; i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefixLen++;
            else break;
        }

        return jaroScore + (prefixLen * 0.1 * (1.0 - jaroScore));
    }

    public boolean isInitialsExpansionMatch(String normA, String normB) {
        if (normA == null || normB == null) return false;

        List<String> tA = tokenize(normA);
        List<String> tB = tokenize(normB);

        // direction A: tA is abbreviated, tB is full
        if (matchInitials(tA, tB)) return true;

        // direction B: tB is abbreviated, tA is full
        if (matchInitials(tB, tA)) return true;

        return false;
    }
    
    private boolean matchInitials(List<String> abbr, List<String> full) {

        // RULE 1: both must have at least 2 tokens
        if (abbr.size() < 2 || full.size() < 2) return false;

        // RULE 2: first token must match
        if (!tokensMatch(abbr.get(0), full.get(0))) return false;

        // RULE 3: last token must match
        if (!tokensMatch(abbr.get(abbr.size() - 1), full.get(full.size() - 1))) return false;

        // RULE 4: middle tokens (indices 1 .. size-2)
        List<String> abbrMiddle = abbr.subList(1, abbr.size() - 1);
        List<String> fullMiddle = full.subList(1, full.size() - 1);

        return middleTokensConsistent(abbrMiddle, fullMiddle);
    }

    private boolean middleTokensConsistent(List<String> abbrMid, List<String> fullMid) {

        // Case A: both empty
        if (abbrMid.isEmpty() && fullMid.isEmpty()) return true;

        // Case B: one side has no middle tokens - the other's must all be initials
        if (abbrMid.isEmpty()) {
            return fullMid.stream().allMatch(this::isInitial);
        }
        if (fullMid.isEmpty()) {
            return abbrMid.stream().allMatch(this::isInitial);
        }

        // Case C: both have middle tokens - align pairwise
        int minLen = Math.min(abbrMid.size(), fullMid.size());
        for (int i = 0; i < minLen; i++) {
            if (!tokensMatch(abbrMid.get(i), fullMid.get(i))) return false;
        }

        // Any excess tokens on either side must be initials
        if (abbrMid.size() > fullMid.size()) {
            for (int i = minLen; i < abbrMid.size(); i++) {
                if (!isInitial(abbrMid.get(i))) return false;
            }
        } else if (fullMid.size() > abbrMid.size()) {
            for (int i = minLen; i < fullMid.size(); i++) {
                if (!isInitial(fullMid.get(i))) return false;
            }
        }

        return true;
    }

    private boolean tokensMatch(String a, String b) {
        boolean aIsInitial = isInitial(a);
        boolean bIsInitial = isInitial(b);

        if (!aIsInitial && !bIsInitial) {
            // Both full tokens - exact equality required
            return a.equals(b);
        }
        if (aIsInitial && bIsInitial) {
            // Both initials - first letters must match
            return a.charAt(0) == b.charAt(0);
        }
        // One initial, one full - initial's letter must equal full token's first char
        String initial = aIsInitial ? a : b;
        String fullTok = aIsInitial ? b : a;
        return !fullTok.isEmpty() && initial.charAt(0) == fullTok.charAt(0);
    }

    /**
     * Core Jaro similarity (no Winkler prefix yet).
     */
    private double jaro(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int matchWindow = Math.max(len1, len2) / 2 - 1;
        if (matchWindow < 0) matchWindow = 0;

        boolean[] s1Matched = new boolean[len1];
        boolean[] s2Matched = new boolean[len2];

        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchWindow);
            int end   = Math.min(i + matchWindow + 1, len2);
            for (int j = start; j < end; j++) {
                if (!s2Matched[j] && s1.charAt(i) == s2.charAt(j)) {
                    s1Matched[i] = true;
                    s2Matched[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) return 0.0;

        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (s1Matched[i]) {
                while (!s2Matched[k]) k++;
                if (s1.charAt(i) != s2.charAt(k)) transpositions++;
                k++;
            }
        }

        return (((double) matches / len1)
                + ((double) matches / len2)
                + ((matches - (double) transpositions / 2.0) / matches)) / 3.0;
    }

    private List<String> tokenize(String name) {
        List<String> tokens = new ArrayList<>();
        for (String t : name.split(" ")) {
            if (!t.isBlank()) tokens.add(t);
        }
        return tokens;
    }

    private boolean isInitial(String token) {
        return token != null && token.matches("^[a-z]\\.?$");
    }


    public record NameMatchResult(
            boolean matched,
            boolean exact,
            double  similarity
    ) {
        public static NameMatchResult noMatch() {
            return new NameMatchResult(false, false, 0.0);
        }

        public static NameMatchResult exactMatch() {
            return new NameMatchResult(true, true, 1.0);
        }

        public static NameMatchResult fuzzy(double similarity) {
            return new NameMatchResult(true, false, similarity);
        }
    }
}