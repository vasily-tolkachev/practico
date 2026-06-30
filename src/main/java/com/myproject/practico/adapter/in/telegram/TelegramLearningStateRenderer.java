package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.learning.state.CompletedActivity;
import com.myproject.practico.application.learning.state.LearningActivity;
import com.myproject.practico.application.learning.state.LearningCardActivity;
import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.PracticeActivity;
import com.myproject.practico.application.learning.state.QuestionActivity;
import com.myproject.practico.application.learning.state.QuickCheckActivity;
import com.myproject.practico.application.learning.state.RetryActivity;
import org.springframework.stereotype.Component;

@Component
public class TelegramLearningStateRenderer {

    public String render(LearningState state) {
        if (state == null) {
            return "No active learning state.";
        }

        StringBuilder sb = new StringBuilder();
        if (state.context() != null && state.context().conceptName() != null) {
            sb.append("Concept: ").append(state.context().conceptName()).append("\n");
        }
        if (state.progress() != null
                && state.progress().conceptOrder() != null
                && state.progress().totalConcepts() != null) {
            sb.append("Progress: ").append(state.progress().conceptOrder()).append("/").append(state.progress().totalConcepts()).append("\n\n");
        }

        LearningActivity activity = state.currentActivity();
        if (activity instanceof QuestionActivity questionActivity) {
            sb.append("Question:\n").append(nullToEmpty(questionActivity.text()));
        } else if (activity instanceof LearningCardActivity learningCardActivity) {
            sb.append("Learning Card:\n");
            if (learningCardActivity.title() != null && !learningCardActivity.title().isBlank()) {
                sb.append(learningCardActivity.title()).append("\n");
            }
            sb.append(nullToEmpty(learningCardActivity.explanation()));
            sb.append("\n\nSend any message to continue.");
        } else if (activity instanceof PracticeActivity practiceActivity) {
            sb.append(formatPractice(practiceActivity));
        } else if (activity instanceof QuickCheckActivity quickCheckActivity) {
            sb.append("Quick Check:\n").append(nullToEmpty(quickCheckActivity.question()));
        } else if (activity instanceof RetryActivity retryActivity) {
            sb.append("Retry:\n").append(nullToEmpty(retryActivity.question()));
        } else if (activity instanceof CompletedActivity) {
            sb.append("Session is completed. Send /start to begin again.");
        } else {
            sb.append("State is unavailable.");
        }

        return sb.toString().trim();
    }

    private String formatPractice(PracticeActivity practiceActivity) {
        StringBuilder sb = new StringBuilder();
        Integer current = practiceActivity.currentItem();
        Integer total = practiceActivity.totalItems();
        sb.append("Practice");
        if (current != null && total != null) {
            sb.append(" ").append(current).append("/").append(total);
        }
        sb.append(":\n");

        if (practiceActivity.items().isEmpty()) {
            sb.append("No practice items available.");
            return sb.toString();
        }

        int index = current == null ? 0 : Math.max(0, Math.min(current - 1, practiceActivity.items().size() - 1));
        PracticeActivity.PracticeItemView item = practiceActivity.items().get(index);
        sb.append(nullToEmpty(item.question()));
        if (item.type() != null && item.type().name().equals("TRUE_FALSE")) {
            sb.append("\n(True/False)");
        } else if (item.options() != null && !item.options().isEmpty()) {
            for (int i = 0; i < item.options().size(); i++) {
                sb.append("\n").append(i + 1).append(") ").append(item.options().get(i));
            }
            sb.append("\n(Reply with number(s): 2 or 1,3)");
        }
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
