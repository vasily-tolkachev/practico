package com.myproject.practico.application.service;

public record QuickCheckRequest(
        String question,
        String expectedAnswer,
        String userAnswer
) {
}
