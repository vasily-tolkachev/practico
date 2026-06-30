package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.PracticeType;

import java.util.List;

public record PracticeActivity(
        Integer currentItem,
        Integer totalItems,
        List<PracticeItemView> items
) implements LearningActivity {

    public PracticeActivity {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public record PracticeItemView(
            PracticeType type,
            String question,
            List<String> options
    ) {
        public PracticeItemView {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }
}
