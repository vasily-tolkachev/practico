package com.myproject.practico.application.service;

import java.util.List;

public record PracticeItem(
        PracticeType type,
        String question,
        List<String> options,
        List<Integer> correctOptions,
        Boolean expectedBoolean
) {
}
