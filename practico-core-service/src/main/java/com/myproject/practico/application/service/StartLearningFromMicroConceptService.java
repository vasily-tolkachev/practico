package com.myproject.practico.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.StartLearningFromMicroConceptUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.MicroConceptContent;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.QuickCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StartLearningFromMicroConceptService implements StartLearningFromMicroConceptUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final QuestionPersistencePort questionPersistencePort;
    private final LearningSessionService learningSessionService;
    private final LearningStateAssembler learningStateAssembler;
    private final RuntimeContextStore runtimeContextStore;
    private final ObjectMapper objectMapper;

    public StartLearningFromMicroConceptService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            QuestionPersistencePort questionPersistencePort,
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler,
            RuntimeContextStore runtimeContextStore,
            ObjectMapper objectMapper
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.questionPersistencePort = questionPersistencePort;
        this.learningSessionService = learningSessionService;
        this.learningStateAssembler = learningStateAssembler;
        this.runtimeContextStore = runtimeContextStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<LearningState> start(Long programId, Long microConceptId, String userId) {
        if (!isValidId(programId) || !isValidId(microConceptId) || isBlank(userId)) {
            return Optional.empty();
        }
        if (learningProgramPersistencePort.findById(programId).isEmpty()) {
            return Optional.empty();
        }
        boolean belongsToProgram = programMicroConceptReadPort.findByProgramId(programId).stream()
                .anyMatch(target -> microConceptId.equals(target.microConceptId()));
        if (!belongsToProgram) {
            return Optional.empty();
        }
        MicroConceptContent content = microConceptContentPersistencePort
                .findByProgramIdAndMicroConceptId(programId, microConceptId)
                .orElse(null);
        if (content == null || content.status() != MicroConceptContentStatus.READY) {
            return Optional.empty();
        }

        Question firstQuestion = questionPersistencePort.findAll().stream()
                .filter(question -> question.microConcept() != null && microConceptId.equals(question.microConcept().id()))
                .findFirst()
                .orElse(null);
        if (firstQuestion == null || firstQuestion.id() == null || firstQuestion.concept() == null || firstQuestion.concept().id() == null) {
            return Optional.empty();
        }

        LearningCycle cycle = cycleFromContent(content);
        runtimeContextStore.bindProgram(userId.trim(), String.valueOf(programId));
        learningSessionService.startLearningSession(userId.trim(), firstQuestion.concept().id(), firstQuestion.id());
        learningSessionService.setCurrentCycle(userId.trim(), cycle);
        learningSessionService.setPhase(userId.trim(), LearningPhase.LEARNING_CARD);
        return Optional.of(learningStateAssembler.assemble(userId.trim(), learningSessionService.getSession(userId.trim()).orElse(null)));
    }

    private LearningCycle cycleFromContent(MicroConceptContent content) {
        return new LearningCycle(
                parseLearningCard(content.learningCardPayload()),
                parseQuickCheck(content.quickCheckPayload()),
                parsePracticeItems(content.practicePayload()),
                parseRetryRubric(content.retryPayload()),
                parseRetryQuestion(content.retryPayload())
        );
    }

    private LearningCard parseLearningCard(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) return null;
        String title = text(node, "title");
        String explanation = text(node, "explanation");
        if (isBlank(title) && isBlank(explanation)) return null;
        return new LearningCard(title, explanation);
    }

    private QuickCheck parseQuickCheck(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) return null;
        String question = text(node, "question");
        String expectedAnswer = text(node, "expectedAnswer");
        if (isBlank(question) && isBlank(expectedAnswer)) return null;
        return new QuickCheck(question, expectedAnswer);
    }

    private List<PracticeItem> parsePracticeItems(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isArray()) return List.of();
        List<PracticeItem> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || !item.isObject()) continue;
            PracticeType type = parseType(text(item, "type"));
            if (type == null) continue;
            String question = text(item, "question");
            List<String> options = textArray(item.get("options"));
            List<Integer> correctOptions = intArray(item.get("correctOptions"));
            Boolean expectedBoolean = booleanOrNull(item.get("expectedBoolean"));
            Boolean ambiguous = booleanOrNull(item.get("ambiguousIndexing"));
            result.add(new PracticeItem(type, question, options, correctOptions, expectedBoolean, ambiguous));
        }
        return result;
    }

    private List<String> parseRetryRubric(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) return List.of();
        return textArray(node.get("rubric"));
    }

    private String parseRetryQuestion(String payload) {
        JsonNode node = readJson(payload);
        if (node == null || !node.isObject()) return null;
        return text(node, "question");
    }

    private JsonNode readJson(String payload) {
        if (isBlank(payload)) return null;
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || field == null) return null;
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return null;
        String result = value.asText();
        return result == null ? null : result.trim();
    }

    private List<String> textArray(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) continue;
            String value = item.asText();
            if (value != null && !value.isBlank()) result.add(value.trim());
        }
        return List.copyOf(result);
    }

    private List<Integer> intArray(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        List<Integer> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) continue;
            if (item.canConvertToInt()) {
                result.add(item.asInt());
                continue;
            }
            try {
                result.add(Integer.parseInt(item.asText().trim()));
            } catch (Exception ignored) {
            }
        }
        return List.copyOf(result);
    }

    private Boolean booleanOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isBoolean()) return node.asBoolean();
        String value = node.asText();
        if (value == null) return null;
        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized)) return true;
        if ("false".equals(normalized)) return false;
        return null;
    }

    private PracticeType parseType(String value) {
        if (isBlank(value)) return null;
        try {
            return PracticeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isValidId(Long value) {
        return value != null && value > 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
