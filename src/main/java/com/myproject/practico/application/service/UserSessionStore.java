package com.myproject.practico.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionStore {

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void put(String userId, Long questionId) {
        sessions.put(userId, new UserSession(userId, questionId));
    }

    public Optional<UserSession> get(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public record UserSession(
            String userId,
            Long currentQuestionId
    ) {}
}
