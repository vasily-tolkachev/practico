package com.myproject.practico.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.LearningProfilePersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.domain.Answer;
import com.myproject.practico.domain.LearningProfile;
import com.myproject.practico.domain.MicroConceptContent;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;
import com.myproject.practico.domain.RuntimeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SubmitAnswerService implements SubmitAnswerUseCase {

    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningEngine learningEngine;
    private final LearningProfilePersistencePort learningProfilePersistencePort;
    private final AnswerPersistencePort answerPersistencePort;
    private final LearningStateAssembler learningStateAssembler;
    private final RuntimeContextStore runtimeContextStore;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final ObjectMapper objectMapper;

    public SubmitAnswerService(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            LearningEngine learningEngine,
            LearningProfilePersistencePort learningProfilePersistencePort,
            AnswerPersistencePort answerPersistencePort,
            LearningStateAssembler learningStateAssembler,
            RuntimeContextStore runtimeContextStore,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            ObjectMapper objectMapper
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningEngine = learningEngine;
        this.learningProfilePersistencePort = learningProfilePersistencePort;
        this.answerPersistencePort = answerPersistencePort;
        this.learningStateAssembler = learningStateAssembler;
        this.runtimeContextStore = runtimeContextStore;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.objectMapper = objectMapper;
    }

    @Override
    public LearningState submit(String userId, String answer) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }
        if (session.phase() == LearningPhase.QUESTION && session.currentCycle() != null) {
            learningSessionService.setPhase(userId, LearningPhase.LEARNING_CARD);
            return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
        }
        if (answer == null || answer.isBlank()) {
            return learningStateAssembler.assemble(userId, session);
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        if (currentQuestion == null) {
            return learningStateAssembler.assemble(userId, session);
        }

        Instant now = Instant.now();
        UUID parsedUserId = parseUserId(userId);
        LearningProfile profile = learningProfilePersistencePort.ensureExists(parsedUserId, now);
        learningProfilePersistencePort.touch(parsedUserId, now);

        LearningResult learningResult;
        try {
            learningResult = session.phase() == LearningPhase.RETRY
                    ? learningEngine.handleRetryAnswer(profile.id(), currentQuestion, answer, session, now)
                    : learningEngine.handleQuestionAnswer(profile.id(), currentQuestion, answer, session, now);
        } catch (IllegalStateException ex) {
            return learningStateAssembler.assemble(userId, session);
        }

        EvaluationResult evaluation = learningResult.evaluation();
        Question nextQuestion = learningResult.nextQuestion();
        persistAnswer(profile, currentQuestion, answer, evaluation, now);

        Long nextQuestionId = switch (learningResult.nextPhase()) {
            case LEARNING_CARD -> currentQuestion.id();
            case QUESTION -> nextQuestion == null ? null : nextQuestion.id();
            case COMPLETED -> null;
            case PRACTICE, QUICK_CHECK, RETRY -> currentQuestion.id();
        };

        learningSessionService.recordAnswerAndSetNextQuestion(userId, evaluation.score(), nextQuestionId);
        learningSessionService.setPhase(userId, learningResult.nextPhase());
        markCurrentMicroConceptIfCompleted(userId, currentQuestion, learningResult.nextQuestion(), learningResult.nextPhase());
        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            LearningCycle fallbackCycle = new LearningCycle(
                    evaluation.learningCard(),
                    evaluation.quickCheck(),
                    null,
                    evaluation.practiceItems(),
                    evaluation.retryRubric(),
                    evaluation.retryQuestion(),
                    null
            );
            LearningCycle cycle = cycleFromStoredContent(userId, currentQuestion).orElse(fallbackCycle);
            learningSessionService.setCurrentCycle(userId, cycle);
        } else if (learningResult.nextPhase() == LearningPhase.RETRY) {
            learningSessionService.setCurrentCycle(userId, session.currentCycle());
        } else {
            learningSessionService.setCurrentCycle(userId, null);
        }
        if (learningResult.nextPhase() == LearningPhase.QUESTION && nextQuestion != null && nextQuestion.concept() != null) {
            learningSessionService.setCurrentQuestion(userId, nextQuestion.concept().id(), nextQuestion.id());
        } else if (learningResult.nextPhase() == LearningPhase.COMPLETED) {
            learningSessionService.setCurrentQuestion(userId, null, null);
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }

    private void markCurrentMicroConceptIfCompleted(
            String userId,
            Question currentQuestion,
            Question nextQuestion,
            LearningPhase nextPhase
    ) {
        if (currentQuestion == null || currentQuestion.microConcept() == null || currentQuestion.microConcept().id() == null) {
            return;
        }

        Long currentMicroConceptId = currentQuestion.microConcept().id();
        if (nextPhase == LearningPhase.COMPLETED) {
            learningSessionService.markMicroConceptMastered(userId, currentMicroConceptId);
            return;
        }

        if (nextPhase != LearningPhase.QUESTION || nextQuestion == null || nextQuestion.microConcept() == null || nextQuestion.microConcept().id() == null) {
            return;
        }

        Long nextMicroConceptId = nextQuestion.microConcept().id();
        if (!currentMicroConceptId.equals(nextMicroConceptId)) {
            learningSessionService.markMicroConceptMastered(userId, currentMicroConceptId);
        }
    }

    private void persistAnswer(LearningProfile profile, Question currentQuestion, String answerText, EvaluationResult evaluation, Instant now) {
        answerPersistencePort.save(new Answer(
                null,
                profile.id(),
                currentQuestion.id(),
                answerText,
                evaluation.score(),
                evaluation.evaluation(),
                now
        ));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid authenticated user id: " + userId, ex);
        }
    }

    private Optional<LearningCycle> cycleFromStoredContent(String userId, Question currentQuestion) {
        Long microConceptId = currentQuestion == null || currentQuestion.microConcept() == null
                ? null
                : currentQuestion.microConcept().id();
        if (microConceptId == null || microConceptId <= 0) {
            return Optional.empty();
        }
        Long programId = runtimeContextStore.get(userId)
                .map(RuntimeContext::programId)
                .flatMap(this::safeParseLong)
                .orElse(null);
        if (programId == null || programId <= 0) {
            return Optional.empty();
        }

        MicroConceptContent content = microConceptContentPersistencePort
                .findByProgramIdAndMicroConceptId(programId, microConceptId)
                .orElse(null);
        if (content == null || content.status() != MicroConceptContentStatus.READY) {
            return Optional.empty();
        }

        LearningCard card = parseLearningCard(content.learningCardPayload());
        QuickCheck quickCheck = parseQuickCheck(content.quickCheckPayload());
        PracticeItem quickCheckItem = parsePracticeItem(content.quickCheckPayload());
        List<PracticeItem> practiceItems = parsePracticeItems(content.practicePayload());
        RetryPayload retryPayload = parseRetryPayload(content.retryPayload());
        if (card == null
                && quickCheck == null
                && quickCheckItem == null
                && practiceItems.isEmpty()
                && retryPayload.rubric().isEmpty()
                && isBlank(retryPayload.question())
                && retryPayload.item() == null) {
            return Optional.empty();
        }

        return Optional.of(new LearningCycle(
                card,
                quickCheck,
                quickCheckItem,
                practiceItems,
                retryPayload.rubric(),
                retryPayload.question(),
                retryPayload.item()
        ));
    }

    private LearningCard parseLearningCard(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) {
            return null;
        }
        String title = text(node, "title");
        String explanation = text(node, "explanation");
        if (isBlank(title) && isBlank(explanation)) {
            return null;
        }
        return new LearningCard(title, explanation);
    }

    private QuickCheck parseQuickCheck(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) {
            return null;
        }
        String question = text(node, "question");
        String expectedAnswer = text(node, "expectedAnswer");
        if (isBlank(question) && isBlank(expectedAnswer)) {
            return null;
        }
        return new QuickCheck(question, expectedAnswer);
    }

    private List<PracticeItem> parsePracticeItems(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<PracticeItem> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || !item.isObject()) {
                continue;
            }
            String typeRaw = text(item, "type");
            PracticeType type = parsePracticeType(typeRaw);
            if (type == null) {
                continue;
            }
            String question = text(item, "question");
            List<String> options = textArray(item.get("options"));
            List<Integer> correctOptions = intArray(item.get("correctOptions"));
            Boolean expectedBoolean = booleanOrNull(item.get("expectedBoolean"));
            List<Integer> correctOrder = intArray(item.get("correctOrder"));
            List<String> leftItems = textArray(item.get("leftItems"));
            List<String> rightItems = textArray(item.get("rightItems"));
            Map<Integer, Integer> correctMatches = intMap(item.get("correctMatches"));
            Boolean ambiguousIndexing = booleanOrNull(item.get("ambiguousIndexing"));
            result.add(new PracticeItem(
                    type,
                    question,
                    options,
                    correctOptions,
                    expectedBoolean,
                    correctOrder,
                    leftItems,
                    rightItems,
                    correctMatches,
                    ambiguousIndexing
            ));
        }
        return result;
    }

    private RetryPayload parseRetryPayload(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) {
            return new RetryPayload(List.of(), null, null);
        }
        List<String> rubric = textArray(node.get("rubric"));
        String question = text(node, "question");
        PracticeItem item = parsePracticeItem(node);
        return new RetryPayload(rubric, question, item);
    }

    private PracticeItem parsePracticeItem(String payload) {
        return parsePracticeItem(readJson(payload));
    }

    private PracticeItem parsePracticeItem(JsonNode item) {
        if (item == null || !item.isObject()) {
            return null;
        }
        PracticeType type = parsePracticeType(text(item, "type"));
        if (type == null) {
            return null;
        }
        String question = text(item, "question");
        List<String> options = textArray(item.get("options"));
        List<Integer> correctOptions = intArray(item.get("correctOptions"));
        Boolean expectedBoolean = booleanOrNull(item.get("expectedBoolean"));
        List<Integer> correctOrder = intArray(item.get("correctOrder"));
        List<String> leftItems = textArray(item.get("leftItems"));
        List<String> rightItems = textArray(item.get("rightItems"));
        Map<Integer, Integer> correctMatches = intMap(item.get("correctMatches"));
        Boolean ambiguousIndexing = booleanOrNull(item.get("ambiguousIndexing"));
        return new PracticeItem(
                type,
                question,
                options,
                correctOptions,
                expectedBoolean,
                correctOrder,
                leftItems,
                rightItems,
                correctMatches,
                ambiguousIndexing
        );
    }

    private JsonNode readJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            return null;
        }
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || fieldName == null) {
            return null;
        }
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        String result = value.asText();
        return result == null ? null : result.trim();
    }

    private List<String> textArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) {
                continue;
            }
            String value = item.asText();
            if (value != null && !value.isBlank()) {
                result.add(value.trim());
            }
        }
        return Collections.unmodifiableList(result);
    }

    private List<Integer> intArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.canConvertToInt()) {
                result.add(item.asInt());
                continue;
            }
            try {
                result.add(Integer.parseInt(item.asText().trim()));
            } catch (Exception ignored) {
            }
        }
        return Collections.unmodifiableList(result);
    }

    private Boolean booleanOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        String value = node.asText();
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        return null;
    }

    private PracticeType parsePracticeType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PracticeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Map<Integer, Integer> intMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        Map<Integer, Integer> result = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            Integer key = safeParseInt(entry.getKey());
            Integer value = safeParseInt(entry.getValue() == null ? null : entry.getValue().asText());
            if (key != null && value != null) {
                result.put(key, value);
            }
        });
        return Collections.unmodifiableMap(result);
    }

    private Integer safeParseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Optional<Long> safeParseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RetryPayload(
            List<String> rubric,
            String question,
            PracticeItem item
    ) {
    }
}
