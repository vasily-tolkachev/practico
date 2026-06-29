package com.myproject.practico.application.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PracticeService {

    public PracticeCheckResult check(String userAnswer, PracticeItem item) {
        if (item == null || item.type() == null) {
            return new PracticeCheckResult(false, "Practice item is unavailable.");
        }

        return switch (item.type()) {
            case TRUE_FALSE -> checkTrueFalse(userAnswer, item);
            case MULTIPLE_CHOICE -> checkMultipleChoice(userAnswer, item);
        };
    }

    private PracticeCheckResult checkTrueFalse(String userAnswer, PracticeItem item) {
        if (item.expectedBoolean() == null) {
            return new PracticeCheckResult(false, "Practice answer is unavailable.");
        }
        Boolean value = parseBoolean(userAnswer);
        if (value == null) {
            return new PracticeCheckResult(false, "Please answer True/False (or Yes/No).");
        }
        if (value.equals(item.expectedBoolean())) {
            return new PracticeCheckResult(true, "Correct.");
        }
        return new PracticeCheckResult(false, "Not quite. Try again.");
    }

    private PracticeCheckResult checkMultipleChoice(String userAnswer, PracticeItem item) {
        if (item.correctOptions() == null || item.correctOptions().isEmpty()) {
            return new PracticeCheckResult(false, "Practice answer is unavailable.");
        }
        Set<Integer> selected = parseSelectedOptions(userAnswer);
        if (selected.isEmpty()) {
            return new PracticeCheckResult(false, "Please answer with option letters (for example: A or A,C).");
        }
        Set<Integer> expected = new HashSet<>(item.correctOptions());
        if (selected.equals(expected)) {
            return new PracticeCheckResult(true, "Correct.");
        }
        return new PracticeCheckResult(false, "Not quite. Try again.");
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "t", "yes", "y", "1" -> Boolean.TRUE;
            case "false", "f", "no", "n", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    private Set<Integer> parseSelectedOptions(String value) {
        Set<Integer> selected = new HashSet<>();
        if (value == null || value.isBlank()) {
            return selected;
        }

        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        String[] tokens = normalized.split("[,;/|]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            // single letter option like A/B/C
            if (token.length() == 1 && token.charAt(0) >= 'A' && token.charAt(0) <= 'Z') {
                selected.add((token.charAt(0) - 'A') + 1);
                continue;
            }
            // fallback numeric option index
            try {
                selected.add(Integer.parseInt(token));
            } catch (NumberFormatException ignored) {
                // ignore invalid token
            }
        }
        return selected;
    }
}
