package com.myproject.practico.domain;

public record Question(
        Long id,
        String text,
        String topic,
        String difficulty
) {}
