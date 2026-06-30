package com.myproject.practico.config;

public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    public static final class Runtime {
        public static final int ZERO_INDEX = 0;
        public static final int ONE_BASED_OFFSET = 1;
        public static final int SCHEMA_VERSION = 1;

        private Runtime() {
        }
    }

    public static final class Scores {
        public static final int MIN = 0;
        public static final int MAX = 10;
        public static final int ANSWERED_THRESHOLD = 6;
        public static final int MASTERY_THRESHOLD = 8;
        public static final int IN_PROGRESS_MIN = 5;

        private Scores() {
        }
    }

    public static final class DifficultyTuning {
        public static final int ANSWERS_FOR_MEDIUM = 2;
        public static final int ANSWERS_FOR_HARD = 3;
        public static final double PROMOTION_AVERAGE_THRESHOLD = 8.0;
        public static final int EASY_INDEX = 0;
        public static final int MEDIUM_INDEX = 1;
        public static final int HARD_INDEX = 2;

        private DifficultyTuning() {
        }
    }

    public static final class SessionLimits {
        public static final int MAX_LAST_SCORES = 3;
        public static final int MAX_ANSWERED_IDS = 100;
        public static final int MAX_MASTERED_MICRO_CONCEPT_IDS = 200;

        private SessionLimits() {
        }
    }
}
