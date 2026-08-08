package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.service.PracticeAnswer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TelegramPracticeAnswerParser {

    public PracticeAnswer parse(String text) {
        return new PracticeAnswer(parseBoolean(text), parseSelectedOptions(text), java.util.List.of(), java.util.Map.of());
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "t", "yes", "y", "1", "да", "д", "верно", "в" -> Boolean.TRUE;
            case "false", "f", "no", "n", "0", "нет", "н", "неверно" -> Boolean.FALSE;
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
