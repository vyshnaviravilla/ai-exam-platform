package com.examplatform.plagiarism;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlagiarismDetectionService {

    private static final double SIMILARITY_THRESHOLD = 0.75;

    public PlagiarismResult checkSimilarity(String code1, String code2) {
        // Preprocess both codes
        String normalized1 = normalizeCode(code1);
        String normalized2 = normalizeCode(code2);
        
        // Calculate similarity using multiple methods
        double stringSim = calculateStringSimilarity(normalized1, normalized2);
        double tokenSim = calculateTokenSimilarity(code1, code2);
        double structureSim = calculateStructureSimilarity(code1, code2);
        
        // Weighted average
        double overallScore = (stringSim * 0.3) + (tokenSim * 0.5) + (structureSim * 0.2);
        
        return PlagiarismResult.builder()
                .similarityScore(overallScore)
                .isPlagiarized(overallScore > SIMILARITY_THRESHOLD)
                .stringSimilarity(stringSim)
                .tokenSimilarity(tokenSim)
                .structureSimilarity(structureSim)
                .details(generateDetails(normalized1, normalized2, overallScore))
                .build();
    }

    private String normalizeCode(String code) {
        return code.toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("//.*", "")
                .replaceAll("\\b(public|private|protected|static|final|abstract)\\b", "")
                .replaceAll("\\b(int|long|double|float|boolean|char|String)\\b", "TYPE")
                .replaceAll("\\b(for|while|if|else|return|class|void)\\b", "KEYWORD")
                .trim();
    }

    private double calculateStringSimilarity(String s1, String s2) {
        // Levenshtein distance normalized
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], 
                                   Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    private double calculateTokenSimilarity(String code1, String code2) {
        // Tokenize and compare using Jaccard similarity
        Set<String> tokens1 = tokenize(code1);
        Set<String> tokens2 = tokenize(code2);
        
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);
        
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String code) {
        return Arrays.stream(code.split("\\W+"))
                .filter(t -> t.length() > 1)
                .filter(t -> !isCommonKeyword(t))
                .collect(Collectors.toSet());
    }

    private boolean isCommonKeyword(String token) {
        Set<String> keywords = Set.of("int", "long", "double", "float", "boolean", 
                "char", "byte", "short", "if", "else", "for", "while", "do", "return",
                "class", "public", "private", "protected", "static", "final", "void",
                "new", "this", "super", "try", "catch", "throw", "throws", "import",
                "package", "true", "false", "null", "abstract", "interface", "extends",
                "implements", "switch", "case", "default", "break", "continue", "goto",
                "synchronized", "volatile", "transient", "strictfp", "finally", "const",
                "native", "assert", "enum", "var");
        return keywords.contains(token.toLowerCase());
    }

    private double calculateStructureSimilarity(String code1, String code2) {
        // Compare code structure patterns (control flow, nesting depth)
        List<String> patterns1 = extractPatterns(code1);
        List<String> patterns2 = extractPatterns(code2);
        
        if (patterns1.isEmpty() || patterns2.isEmpty()) return 0.0;
        
        int matches = 0;
        for (String p1 : patterns1) {
            for (String p2 : patterns2) {
                if (p1.equals(p2)) matches++;
            }
        }
        
        return (double) (2 * matches) / (patterns1.size() + patterns2.size());
    }

    private List<String> extractPatterns(String code) {
        List<String> patterns = new ArrayList<>();
        
        // Extract loop patterns
        patterns.addAll(extractPattern(code, "for\\s*\\([^)]*\\)", "FOR"));
        patterns.addAll(extractPattern(code, "while\\s*\\([^)]*\\)", "WHILE"));
        patterns.addAll(extractPattern(code, "if\\s*\\([^)]*\\)", "IF"));
        patterns.addAll(extractPattern(code, "else\\s*if\\s*\\([^)]*\\)", "ELSEIF"));
        
        return patterns;
    }

    private List<String> extractPattern(String code, String regex, String label) {
        List<String> matches = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            matches.add(label);
        }
        return matches;
    }

    private String generateDetails(String code1, String code2, double score) {
        if (score > 0.9) {
            return "Very high similarity detected. Code may be identical or nearly identical.";
        } else if (score > 0.75) {
            return "High similarity detected. Significant portions of code appear similar.";
        } else if (score > 0.5) {
            return "Moderate similarity. Some structural similarities detected.";
        } else {
            return "Low similarity. Code appears to be independently written.";
        }
    }

    public static class PlagiarismResult {
        private double similarityScore;
        private boolean isPlagiarized;
        private double stringSimilarity;
        private double tokenSimilarity;
        private double structureSimilarity;
        private String details;

        public static Builder builder() {
            return new Builder();
        }

        public double getSimilarityScore() { return similarityScore; }
        public boolean isPlagiarized() { return isPlagiarized; }
        public double getStringSimilarity() { return stringSimilarity; }
        public double getTokenSimilarity() { return tokenSimilarity; }
        public double getStructureSimilarity() { return structureSimilarity; }
        public String getDetails() { return details; }

        public static class Builder {
            private PlagiarismResult result = new PlagiarismResult();

            public Builder similarityScore(double score) {
                result.similarityScore = score;
                return this;
            }

            public Builder isPlagiarized(boolean isPlagiarized) {
                result.isPlagiarized = isPlagiarized;
                return this;
            }

            public Builder stringSimilarity(double sim) {
                result.stringSimilarity = sim;
                return this;
            }

            public Builder tokenSimilarity(double sim) {
                result.tokenSimilarity = sim;
                return this;
            }

            public Builder structureSimilarity(double sim) {
                result.structureSimilarity = sim;
                return this;
            }

            public Builder details(String details) {
                result.details = details;
                return this;
            }

            public PlagiarismResult build() {
                return result;
            }
        }
    }
}
