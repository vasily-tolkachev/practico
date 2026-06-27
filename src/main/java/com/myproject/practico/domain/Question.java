package com.myproject.practico.domain;

public record Question(
        Long id,
        String text,
        Concept concept,
        String difficulty
) {}
