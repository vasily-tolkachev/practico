package com.myproject.practico.application.service;

import java.util.Optional;

public class GoalRuntimeBindingService {

    private final GoalRuntimeBindingStore store;

    public GoalRuntimeBindingService(GoalRuntimeBindingStore store) {
        this.store = store;
    }

    public void bind(String userId, Long goalId, String programId) {
        store.bind(userId, goalId, programId);
    }

    public Optional<GoalRuntimeBindingStore.GoalRuntimeBinding> get(String userId) {
        return store.get(userId);
    }
}
