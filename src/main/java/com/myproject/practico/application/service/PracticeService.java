package com.myproject.practico.application.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PracticeService {

    public PracticeCheckResult check(String userAnswer, PracticeItem item) {
        if (item == null || item.type() == null) {
            return new PracticeCheckResult(false, "Задание практики недоступно.");
        }

        return switch (item.type()) {
            case TRUE_FALSE -> checkTrueFalse(userAnswer, item);
            case MULTIPLE_CHOICE -> checkMultipleChoice(userAnswer, item);
        };
    }

    private PracticeCheckResult checkTrueFalse(String userAnswer, PracticeItem item) {
        if (item.expectedBoolean() == null) {
            return new PracticeCheckResult(false, "Ожидаемый ответ для практики недоступен.");
        }
        Boolean value = parseBoolean(userAnswer);
        if (value == null) {
            return new PracticeCheckResult(false, "Ответьте: «верно/неверно» (или «да/нет»).");
        }
        if (value.equals(item.expectedBoolean())) {
            return new PracticeCheckResult(true, "Верно.");
        }
        return new PracticeCheckResult(false, "Пока неверно. Попробуйте ещё раз.");
    }

    private PracticeCheckResult checkMultipleChoice(String userAnswer, PracticeItem item) {
        if (item.correctOptions() == null || item.correctOptions().isEmpty()) {
            return new PracticeCheckResult(false, "Ожидаемый ответ для практики недоступен.");
        }
        Set<Integer> selected = parseSelectedOptions(userAnswer);
        if (selected.isEmpty()) {
            return new PracticeCheckResult(false, "Ответьте номером/номерами варианта (например: 2 или 1,3).");
        }
        Set<Integer> expected = new HashSet<>(item.correctOptions());
        if (selected.equals(expected)) {
            return new PracticeCheckResult(true, "Верно.");
        }

        // Tolerance for ambiguous AI indexing (0-based vs 1-based) only when flagged by parser.
        if (Boolean.TRUE.equals(item.ambiguousIndexing())) {
            Set<Integer> shiftedPlusOne = new HashSet<>();
            for (Integer value : expected) {
                int shifted = value + 1;
                if (item.options() != null && shifted > 0 && shifted <= item.options().size()) {
                    shiftedPlusOne.add(shifted);
                }
            }
            if (!shiftedPlusOne.isEmpty() && selected.equals(shiftedPlusOne)) {
                return new PracticeCheckResult(true, "Верно.");
            }
        }
        return new PracticeCheckResult(false, "Пока неверно. Попробуйте ещё раз.");
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "t", "yes", "y", "1" -> Boolean.TRUE;
            case "false", "f", "no", "n", "0" -> Boolean.FALSE;
            case "верно", "в", "да", "д", "истина" -> Boolean.TRUE;
            case "неверно", "н", "нет", "ложь" -> Boolean.FALSE;
            default -> null;
        };
    }

    private Set<Integer> parseSelectedOptions(String value) {
        Set<Integer> selected = new HashSet<>();
        if (value == null || value.isBlank()) {
            return selected;
        }

        String normalized = value.replaceAll("\\s+", "");
        String[] tokens = normalized.split("[,;/|]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            try {
                selected.add(Integer.parseInt(token));
            } catch (NumberFormatException ignored) {
                // ignore invalid token
            }
        }
        return selected;
    }
}
