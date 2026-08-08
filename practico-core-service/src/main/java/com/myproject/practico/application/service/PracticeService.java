package com.myproject.practico.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PracticeService {

    public PracticeCheckResult check(PracticeAnswer answer, PracticeItem item) {
        if (item == null || item.type() == null) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.ITEM_UNAVAILABLE);
        }

        return switch (item.type()) {
            case TRUE_FALSE -> checkTrueFalse(answer, item);
            case MULTIPLE_CHOICE -> checkMultipleChoice(answer, item);
            case MULTI_SELECT -> checkMultiSelect(answer, item);
            case ORDERING -> checkOrdering(answer, item);
            case MATCHING -> checkMatching(answer, item);
        };
    }

    private PracticeCheckResult checkTrueFalse(PracticeAnswer answer, PracticeItem item) {
        if (item.expectedBoolean() == null) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.EXPECTED_ANSWER_UNAVAILABLE);
        }
        Boolean value = answer == null ? null : answer.booleanAnswer();
        if (value == null) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.INVALID_FORMAT);
        }
        if (value.equals(item.expectedBoolean())) {
            return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
        }
        return new PracticeCheckResult(false, PracticeFeedbackCode.INCORRECT);
    }

    private PracticeCheckResult checkMultipleChoice(PracticeAnswer answer, PracticeItem item) {
        if (item.correctOptions() == null || item.correctOptions().isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.EXPECTED_ANSWER_UNAVAILABLE);
        }
        Set<Integer> selected = answer == null ? Set.of() : answer.selectedOptions();
        if (selected.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.INVALID_FORMAT);
        }
        Set<Integer> expected = Set.copyOf(item.correctOptions());
        if (selected.equals(expected)) {
            return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
        }

        if (Boolean.TRUE.equals(item.ambiguousIndexing())) {
            Set<Integer> shiftedPlusOne = new HashSet<>();
            for (Integer value : expected) {
                int shifted = value + 1;
                if (item.options() != null && shifted > 0 && shifted <= item.options().size()) {
                    shiftedPlusOne.add(shifted);
                }
            }
            if (!shiftedPlusOne.isEmpty() && selected.equals(shiftedPlusOne)) {
                return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
            }
        }
        return new PracticeCheckResult(false, PracticeFeedbackCode.INCORRECT);
    }

    private PracticeCheckResult checkMultiSelect(PracticeAnswer answer, PracticeItem item) {
        if (item.correctOptions() == null || item.correctOptions().isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.EXPECTED_ANSWER_UNAVAILABLE);
        }
        Set<Integer> selected = answer == null ? Set.of() : answer.selectedOptions();
        if (selected.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.INVALID_FORMAT);
        }
        Set<Integer> expected = Set.copyOf(item.correctOptions());
        if (selected.equals(expected)) {
            return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
        }
        return new PracticeCheckResult(false, PracticeFeedbackCode.INCORRECT);
    }

    private PracticeCheckResult checkOrdering(PracticeAnswer answer, PracticeItem item) {
        List<Integer> expectedOrder = item.correctOrder();
        if (expectedOrder == null || expectedOrder.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.EXPECTED_ANSWER_UNAVAILABLE);
        }
        List<Integer> submittedOrder = answer == null ? List.of() : answer.orderedOptions();
        if (submittedOrder.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.INVALID_FORMAT);
        }
        if (submittedOrder.equals(expectedOrder)) {
            return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
        }
        return new PracticeCheckResult(false, PracticeFeedbackCode.INCORRECT);
    }

    private PracticeCheckResult checkMatching(PracticeAnswer answer, PracticeItem item) {
        Map<Integer, Integer> expectedMatches = item.correctMatches();
        if (expectedMatches == null || expectedMatches.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.EXPECTED_ANSWER_UNAVAILABLE);
        }
        Map<Integer, Integer> submittedMatches = answer == null ? Map.of() : answer.matches();
        if (submittedMatches.isEmpty()) {
            return new PracticeCheckResult(false, PracticeFeedbackCode.INVALID_FORMAT);
        }
        if (submittedMatches.equals(expectedMatches)) {
            return new PracticeCheckResult(true, PracticeFeedbackCode.CORRECT);
        }
        return new PracticeCheckResult(false, PracticeFeedbackCode.INCORRECT);
    }
}
