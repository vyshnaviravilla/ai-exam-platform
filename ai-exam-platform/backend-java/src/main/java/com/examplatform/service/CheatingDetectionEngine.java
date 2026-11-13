package com.examplatform.service;

import com.examplatform.model.ExamSession;
import com.examplatform.model.SecurityViolation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheatingDetectionEngine {

    // Risk score weights
    private static final int TAB_SWITCH_WEIGHT = 15;
    private static final int FULLSCREEN_EXIT_WEIGHT = 30;
    private static final int MULTIPLE_FACES_WEIGHT = 40;
    private static final int NO_FACE_WEIGHT = 20;
    private static final int LOOKING_AWAY_WEIGHT = 10;
    private static final int SUSPICIOUS_PROCESS_WEIGHT = 25;
    private static final int DEVTOOLS_ATTEMPT_WEIGHT = 20;

    public CheatingScore calculateRiskScore(List<SecurityViolation> violations, int aiProctoringScore) {
        int baseScore = 0;
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;

        for (SecurityViolation violation : violations) {
            switch (violation.getSeverity()) {
                case CRITICAL -> {
                    criticalCount++;
                    baseScore += getCriticalWeight(violation.getType());
                }
                case HIGH -> {
                    highCount++;
                    baseScore += getHighWeight(violation.getType());
                }
                case MEDIUM -> {
                    mediumCount++;
                    baseScore += getMediumWeight(violation.getType());
                }
                case LOW -> baseScore += 5;
            }
        }

        // Combine with AI proctoring score
        int combinedScore = Math.min(100, (baseScore + aiProctoringScore) / 2);
        
        // Determine severity level
        RiskLevel level;
        if (combinedScore >= 70) {
            level = RiskLevel.HIGH;
        } else if (combinedScore >= 40) {
            level = RiskLevel.MEDIUM;
        } else if (combinedScore >= 20) {
            level = RiskLevel.LOW;
        } else {
            level = RiskLevel.MINIMAL;
        }

        return CheatingScore.builder()
                .score(combinedScore)
                .level(level)
                .criticalViolations(criticalCount)
                .highViolations(highCount)
                .mediumViolations(mediumCount)
                .flaggedForReview(combinedScore >= 50)
                .autoDisqualified(criticalCount >= 3 || combinedScore >= 85)
                .build();
    }

    private int getCriticalWeight(String type) {
        if (type.contains("Multiple faces")) return MULTIPLE_FACES_WEIGHT;
        if (type.contains("Fullscreen exited")) return FULLSCREEN_EXIT_WEIGHT;
        if (type.contains("DevTools")) return DEVTOOLS_ATTEMPT_WEIGHT;
        return 35;
    }

    private int getHighWeight(String type) {
        if (type.contains("Tab switch")) return TAB_SWITCH_WEIGHT;
        if (type.contains("Face not detected")) return NO_FACE_WEIGHT;
        if (type.contains("Suspicious process")) return SUSPICIOUS_PROCESS_WEIGHT;
        return 20;
    }

    private int getMediumWeight(String type) {
        if (type.contains("Looking away")) return LOOKING_AWAY_WEIGHT;
        return 10;
    }

    public boolean shouldAutoSubmit(CheatingScore score) {
        return score.isAutoDisqualified() || score.getScore() >= 85;
    }

    public String generateWarningMessage(CheatingScore score) {
        return switch (score.getLevel()) {
            case MINIMAL -> "Exam proceeding normally.";
            case LOW -> "Minor irregularities detected. Please stay focused on the exam.";
            case MEDIUM -> "Multiple anomalies detected. Please remain in full-screen mode and avoid switching tabs.";
            case HIGH -> "Serious violations detected. Your exam may be flagged for review.";
        };
    }

    public static class CheatingScore {
        private int score;
        private RiskLevel level;
        private int criticalViolations;
        private int highViolations;
        private int mediumViolations;
        private boolean flaggedForReview;
        private boolean autoDisqualified;

        public static Builder builder() {
            return new Builder();
        }

        public int getScore() { return score; }
        public RiskLevel getLevel() { return level; }
        public int getCriticalViolations() { return criticalViolations; }
        public int getHighViolations() { return highViolations; }
        public int getMediumViolations() { return mediumViolations; }
        public boolean isFlaggedForReview() { return flaggedForReview; }
        public boolean isAutoDisqualified() { return autoDisqualified; }

        public static class Builder {
            private CheatingScore score = new CheatingScore();

            public Builder score(int score) {
                this.score.score = score;
                return this;
            }

            public Builder level(RiskLevel level) {
                this.score.level = level;
                return this;
            }

            public Builder criticalViolations(int count) {
                this.score.criticalViolations = count;
                return this;
            }

            public Builder highViolations(int count) {
                this.score.highViolations = count;
                return this;
            }

            public Builder mediumViolations(int count) {
                this.score.mediumViolations = count;
                return this;
            }

            public Builder flaggedForReview(boolean flagged) {
                this.score.flaggedForReview = flagged;
                return this;
            }

            public Builder autoDisqualified(boolean disqualified) {
                this.score.autoDisqualified = disqualified;
                return this;
            }

            public CheatingScore build() {
                return score;
            }
        }
    }

    public enum RiskLevel {
        MINIMAL, LOW, MEDIUM, HIGH
    }
}
