package com.myproject.practico.adapter.out.openai;

import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.port.out.QuickCheckPort;
import com.myproject.practico.application.service.EvaluationRequest;
import com.myproject.practico.application.service.EvaluationResult;
import com.myproject.practico.application.service.PracticeItem;
import com.myproject.practico.application.service.PracticeType;
import com.myproject.practico.application.service.QuickCheckRequest;
import com.myproject.practico.application.service.QuickCheckResult;
import com.myproject.practico.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiClient implements EvaluationPort, QuickCheckPort {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 10;
    private static final int ANSWERED_SCORE_THRESHOLD = 6;

    private static final String PROMPT = """
            Ты технический наставник для начинающих.
            Пользователь изучает этот концепт впервые.
            Не оценивай как на собеседовании.
            Поощряй частичное понимание.
            Если пользователь уловил основную идею, считай это хорошим учебным ответом.
            Цель — прогресс в обучении, а не отбор кандидатов.
            Отвечай пользователю строго на русском языке. Игнорируй опечатки.
            Оценивай ТОЛЬКО знания, необходимые для ответа на заданный вопрос.
            Не требуй информацию, о которой не спрашивали.
            Не награждай за лишние детали.
            Не штрафуй за отсутствие дополнительных деталей, если на вопрос отвечено корректно.
            Используй тип вопроса для глубины:
            - DEFINITION: только краткое определение.
            - UNDERSTANDING: короткое «почему/как» по сути вопроса.
            - APPLICATION: практическое применение в контексте.
            - COMPARISON: ключевое различие только между запрошенными вариантами.

            Шкала:
            0-2: пока не понимает
            3-5: есть частичное понимание
            6-7: основная идея понята
            8-10: отличное объяснение

            Верни только JSON в этой форме:

            {
              "score": number from 0 to 10,
              "answeredQuestion": true or false,
              "evaluation": "короткая конструктивная обратная связь на русском",
              "learningCard": {
                "title": "короткий заголовок на русском",
                "explanation": "очень короткое объяснение того, чего не хватило, на русском"
              },
              "quickCheck": {
                "question": "ОДИН очень короткий проверочный вопрос на русском",
                "expectedAnswer": "короткий ожидаемый ответ на русском"
              },
              "practice": [
                {
                  "type": "TRUE_FALSE",
                  "question": "короткий вопрос на русском",
                  "expectedBoolean": true
                },
                {
                  "type": "MULTIPLE_CHOICE",
                  "question": "короткий вопрос на русском",
                  "options": ["...", "...", "..."],
                  "correctOptions": [2]
                }
              ],
              "retryRubric": ["idea 1", "idea 2"],
              "retryQuestion": "короткий упрощённый повторный вопрос на русском"
            }

            Если answeredQuestion = true, установи "learningCard", "quickCheck", "practice", "retryRubric" и "retryQuestion" в null.
            Поле "evaluation" должно быть коротким, поддерживающим, максимум 2 предложения.
            Объяснение в learningCard: до 70 слов.
            LearningCard должна объяснять только то, чего не хватило в ответе пользователя.
            Для practice:
            - сгенерируй 3-5 заданий
            - используй только TRUE_FALSE и MULTIPLE_CHOICE
            - каждое задание должно быть коротким
            - в MULTIPLE_CHOICE должно быть 3 или 4 варианта
            - для MULTIPLE_CHOICE correctOptions должен быть 1-based: 1, 2, 3, 4
            Для quickCheck:
            - ровно один очень короткий вопрос
            - лучше true/false, выбор одного варианта или короткое «почему»
            - без длинных объяснений и составных вопросов
            Для retryQuestion:
            - не повторяй исходную формулировку
            - проверяй ту же идею, что в исходном вопросе
            - сделай вопрос проще исходного
            - проверяй только одну ключевую идею
            - максимум 20 слов
            Для retryRubric:
            - верни 2-4 коротких ожидаемых идеи
            - каждая идея должна быть конкретным критерием проверки retry

            Вопрос:
            %s

            Тип вопроса:
            %s

            Ответ пользователя:
            %s
            """;
    private static final String RETRY_PROMPT = """
            Ты оцениваешь повторный ответ после обучения и практики.
            Пользователь уже прочитал карточку и прошёл практику.
            Оценивай только по рубрике retry ниже.
            Пиши ответ пользователю строго на русском языке.

            Повторный вопрос:
            %s

            Тип вопроса:
            %s

            Идеи рубрики retry:
            %s

            Ответ пользователя:
            %s

            Верни только JSON в этой форме:
            {
              "score": number from 0 to 10,
              "answeredQuestion": true or false,
              "evaluation": "короткая конструктивная обратная связь на русском",
              "learningCard": null,
              "quickCheck": null,
              "practice": null,
              "retryRubric": null,
              "retryQuestion": null
            }

            Правила:
            - Ставь answeredQuestion=true, если в ответе корректно отражена хотя бы одна ключевая идея рубрики.
            - Главный критерий — покрытие рубрики, а не длина ответа.
            - Обратная связь короткая (максимум 2 предложения).
            """;
    private static final String QUICK_CHECK_PROMPT = """
            Ты проверяешь короткий проверочный вопрос.
            Пиши ответ пользователю строго на русском языке.

            Вопрос:
            %s

            Ожидаемый ответ:
            %s

            Ответ пользователя:
            %s

            Определи, демонстрирует ли ответ понимание.

            Формулировка не обязана совпадать дословно.
            Принимай синонимы и эквивалентные формулировки.

            Верни только JSON в этой форме:
            {
              "correct": true,
              "feedback": "короткая конструктивная обратная связь на русском"
            }
            """;
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
    private static final Pattern ANSWERED_QUESTION_PATTERN = Pattern.compile("\"answeredQuestion\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVALUATION_PATTERN = Pattern.compile("\"evaluation\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern LEARNING_CARD_TEXT_PATTERN =
            Pattern.compile("\"learningCard\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern LEARNING_CARD_OBJECT_PATTERN =
            Pattern.compile("\"learningCard\"\\s*:\\s*\\{([\\s\\S]*?)\\}");
    private static final Pattern TITLE_PATTERN = Pattern.compile("\"title\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern EXPLANATION_PATTERN = Pattern.compile("\"explanation\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern QUICK_CHECK_OBJECT_PATTERN =
            Pattern.compile("\"quickCheck\"\\s*:\\s*\\{([\\s\\S]*?)\\}");
    private static final Pattern QUICK_CHECK_QUESTION_PATTERN =
            Pattern.compile("\"question\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern QUICK_CHECK_EXPECTED_ANSWER_PATTERN =
            Pattern.compile("\"expectedAnswer\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern RETRY_QUESTION_PATTERN = Pattern.compile("\"retryQuestion\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern RETRY_RUBRIC_PATTERN = Pattern.compile("\"retryRubric\"\\s*:\\s*\\[([\\s\\S]*?)\\]");
    private static final Pattern JSON_STRING_PATTERN = Pattern.compile("\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern QUICK_CHECK_CORRECT_PATTERN = Pattern.compile("\"correct\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUICK_CHECK_FEEDBACK_PATTERN = Pattern.compile("\"feedback\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private final OpenAiProperties properties;

    @Override
    public EvaluationResult evaluate(EvaluationRequest request) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI evaluation skipped: OPENAI_API_KEY is empty");
            return fallback("Оценка ИИ недоступна: OPENAI_API_KEY не настроен.");
        }

        boolean retryMode = request.retryRubric() != null && !request.retryRubric().isEmpty();
        String content = callModel(
                retryMode ? "retry-evaluation" : "evaluation",
                retryMode
                        ? RETRY_PROMPT.formatted(
                        request.question(),
                        request.questionType() == null ? "UNSPECIFIED" : request.questionType().name(),
                        formatRetryRubric(request.retryRubric()),
                        request.answer()
                )
                        : PROMPT.formatted(
                        request.question(),
                        request.questionType() == null ? "UNSPECIFIED" : request.questionType().name(),
                        request.answer()
                )
        );
        if (content == null || content.isBlank()) {
            return fallback("Оценка ИИ временно недоступна. Попробуйте ещё раз.");
        }

        return parseResult(content);
    }

    @Override
    public QuickCheckResult evaluate(QuickCheckRequest request) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI quick check skipped: OPENAI_API_KEY is empty");
            return quickCheckFallback("Мини-проверка временно недоступна.");
        }

        String content = callModel(
                "quick-check",
                QUICK_CHECK_PROMPT.formatted(
                        safeValue(request.question()),
                        safeValue(request.expectedAnswer()),
                        safeValue(request.userAnswer())
                )
        );
        if (content == null || content.isBlank()) {
            return quickCheckFallback("Мини-проверка временно недоступна.");
        }

        return parseQuickCheckResult(content);
    }

    private String callModel(String useCase, String prompt) {
        String model = properties.model() == null || properties.model().isBlank()
                ? "gpt-4.1-mini"
                : properties.model();
        String apiKey = properties.apiKey();

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(new ChatCompletionRequest(
                            model,
                            new Message[]{
                                    new Message("system", "Ты лаконичный и строгий оценщик. Всегда отвечай пользователю на русском языке."),
                                    new Message("user", prompt)
                            },
                            new ResponseFormat("json_object")
                    ))
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().length == 0) {
                log.error("OpenAI {} failed: empty response. model={}", useCase, model);
                return null;
            }

            return response.choices()[0].message().content();
        } catch (RestClientResponseException ex) {
            log.error(
                    "OpenAI {} HTTP error. model={}, status={}, body={}",
                    useCase,
                    model,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
            return null;
        } catch (Exception ex) {
            log.error("OpenAI {} failed. model={}", useCase, model, ex);
            return null;
        }
    }

    private EvaluationResult parseResult(String content) {
        int score = parseScore(content);
        boolean answeredQuestion = parseAnsweredQuestion(content, score);
        String evaluation = parseEvaluation(content);
        LearningCard learningCard = parseLearningCard(content, answeredQuestion, evaluation);
        QuickCheck quickCheck = parseQuickCheck(content);
        List<PracticeItem> practiceItems = parsePracticeItems(content, answeredQuestion);
        List<String> retryRubric = parseRetryRubric(content, answeredQuestion);
        String retryQuestion = parseRetryQuestion(content, answeredQuestion);
        if (answeredQuestion) {
            quickCheck = null;
            practiceItems = List.of();
            retryRubric = List.of();
            retryQuestion = null;
        }

        return new EvaluationResult(score, answeredQuestion, evaluation, learningCard, quickCheck, practiceItems, retryRubric, retryQuestion);
    }

    private int parseScore(String content) {
        Matcher matcher = SCORE_PATTERN.matcher(content);
        if (!matcher.find()) {
            return SCORE_MIN;
        }
        return clampScore(Integer.parseInt(matcher.group(1)));
    }

    private String parseEvaluation(String content) {
        Matcher matcher = EVALUATION_PATTERN.matcher(content);
        if (!matcher.find()) {
            return "Оценка не предоставлена.";
        }
        String value = unescape(matcher.group(1)).trim();
        return value.isEmpty() ? "Оценка не предоставлена." : value;
    }

    private boolean parseAnsweredQuestion(String content, int score) {
        Matcher matcher = ANSWERED_QUESTION_PATTERN.matcher(content);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1).toLowerCase());
        }
        return score >= ANSWERED_SCORE_THRESHOLD;
    }

    private LearningCard parseLearningCard(String content, boolean answeredQuestion, String evaluation) {
        Matcher textMatcher = LEARNING_CARD_TEXT_PATTERN.matcher(content);
        if (textMatcher.find()) {
            String explanation = unescape(textMatcher.group(1)).trim();
            if (!explanation.isEmpty()) {
                return new LearningCard("Ключевая идея", explanation);
            }
        }

        Matcher objectMatcher = LEARNING_CARD_OBJECT_PATTERN.matcher(content);
        if (objectMatcher.find()) {
            String objectContent = objectMatcher.group(1);
            String title = parseField(objectContent, TITLE_PATTERN, "Ключевая идея");
            String explanation = parseField(objectContent, EXPLANATION_PATTERN, "");
            if (!explanation.isBlank()) {
                return new LearningCard(title, explanation);
            }
        }

        if (!answeredQuestion) {
            return new LearningCard("Ключевая идея", evaluation);
        }

        return null;
    }

    private String parseField(String content, Pattern pattern, String fallback) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return fallback;
        }
        String value = unescape(matcher.group(1)).trim();
        return value.isEmpty() ? fallback : value;
    }

    private QuickCheck parseQuickCheck(String content) {
        Matcher objectMatcher = QUICK_CHECK_OBJECT_PATTERN.matcher(content);
        if (!objectMatcher.find()) {
            return null;
        }

        String objectContent = objectMatcher.group(1);
        String question = parseField(objectContent, QUICK_CHECK_QUESTION_PATTERN, "");
        String expectedAnswer = parseField(objectContent, QUICK_CHECK_EXPECTED_ANSWER_PATTERN, "");

        if (question.isBlank()) {
            return null;
        }

        return new QuickCheck(question, expectedAnswer);
    }

    private String parseRetryQuestion(String content, boolean answeredQuestion) {
        if (answeredQuestion) {
            return null;
        }
        String retryQuestion = parseField(content, RETRY_QUESTION_PATTERN, "");
        return retryQuestion.isBlank() ? null : retryQuestion;
    }

    private List<String> parseRetryRubric(String content, boolean answeredQuestion) {
        if (answeredQuestion) {
            return List.of();
        }
        Matcher arrayMatcher = RETRY_RUBRIC_PATTERN.matcher(content);
        if (!arrayMatcher.find()) {
            return List.of();
        }
        Matcher itemMatcher = JSON_STRING_PATTERN.matcher(arrayMatcher.group(1));
        List<String> items = new ArrayList<>();
        while (itemMatcher.find()) {
            String item = unescape(itemMatcher.group(1)).trim();
            if (!item.isBlank()) {
                items.add(item);
            }
        }
        return items;
    }

    private List<PracticeItem> parsePracticeItems(String content, boolean answeredQuestion) {
        if (answeredQuestion) {
            return List.of();
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(content);
            JsonNode practiceNode = root.path("practice");
            if (!practiceNode.isArray()) {
                return List.of();
            }

            List<PracticeItem> items = new ArrayList<>();
            for (JsonNode item : practiceNode) {
                String typeValue = item.path("type").asText("");
                PracticeType type;
                try {
                    type = PracticeType.valueOf(typeValue);
                } catch (Exception ignored) {
                    continue;
                }

                String question = item.path("question").asText("").trim();
                if (question.isBlank()) {
                    continue;
                }

                if (type == PracticeType.TRUE_FALSE) {
                    if (!item.has("expectedBoolean")) {
                        continue;
                    }
                    items.add(new PracticeItem(type, question, List.of(), List.of(), item.path("expectedBoolean").asBoolean(), false));
                    continue;
                }

                JsonNode optionsNode = item.path("options");
                JsonNode correctNode = item.path("correctOptions");
                if (!optionsNode.isArray() || !correctNode.isArray()) {
                    continue;
                }

                List<String> options = new ArrayList<>();
                for (JsonNode optionNode : optionsNode) {
                    String option = optionNode.asText("").trim();
                    if (!option.isBlank()) {
                        options.add(option);
                    }
                }
                if (options.isEmpty()) {
                    continue;
                }

                List<Integer> correctOptions = new ArrayList<>();
                for (JsonNode c : correctNode) {
                    int index = c.asInt(0);
                    if (index >= 0) {
                        correctOptions.add(index);
                    }
                }
                if (correctOptions.isEmpty()) {
                    continue;
                }

                // Normalize clearly 0-based keys to 1-based (A=1, B=2, ...)
                if (correctOptions.stream().anyMatch(i -> i == 0)) {
                    List<Integer> normalized = new ArrayList<>();
                    for (Integer i : correctOptions) {
                        int shifted = i + 1;
                        if (shifted > 0 && shifted <= options.size()) {
                            normalized.add(shifted);
                        }
                    }
                    correctOptions = normalized;
                } else {
                    // keep only valid 1-based indexes
                    List<Integer> normalized = new ArrayList<>();
                    for (Integer i : correctOptions) {
                        if (i > 0 && i <= options.size()) {
                            normalized.add(i);
                        }
                    }
                    correctOptions = normalized;
                }
                if (correctOptions.isEmpty()) {
                    continue;
                }

                boolean containsZero = correctOptions.stream().anyMatch(i -> i == 0);
                boolean containsMax = correctOptions.stream().anyMatch(i -> i == options.size());
                boolean ambiguousIndexing = !containsZero && !containsMax
                        && correctOptions.stream().allMatch(i -> i > 0 && i < options.size());

                items.add(new PracticeItem(type, question, options, correctOptions, null, ambiguousIndexing));
            }
            return items;
        } catch (Exception ex) {
            log.warn("Failed to parse practice items", ex);
            return List.of();
        }
    }

    private QuickCheckResult parseQuickCheckResult(String content) {
        Matcher correctMatcher = QUICK_CHECK_CORRECT_PATTERN.matcher(content);
        boolean correct = correctMatcher.find() && Boolean.parseBoolean(correctMatcher.group(1).toLowerCase());
        String feedback = parseField(content, QUICK_CHECK_FEEDBACK_PATTERN, correct ? "Верно." : "Почти верно. Попробуйте ещё раз.");
        return new QuickCheckResult(correct, feedback);
    }

    private String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private int clampScore(int rawScore) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, rawScore));
    }

    private QuickCheckResult quickCheckFallback(String feedback) {
        return new QuickCheckResult(false, feedback);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String formatRetryRubric(List<String> retryRubric) {
        if (retryRubric == null || retryRubric.isEmpty()) {
            return "- (рубрика не передана)";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : retryRubric) {
            if (item == null || item.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("- ").append(item.trim());
        }
        return builder.length() == 0 ? "- (рубрика не передана)" : builder.toString();
    }

    private EvaluationResult fallback(String evaluation) {
        return new EvaluationResult(
                SCORE_MIN,
                false,
                evaluation,
                new LearningCard("Ключевая идея", "Просмотрите концепт и попробуйте снова."),
                null,
                List.of(),
                List.of(),
                null
        );
    }

    private record ChatCompletionRequest(
            String model,
            Message[] messages,
            ResponseFormat response_format
    ) {}

    private record Message(
            String role,
            String content
    ) {}

    private record ResponseFormat(
            String type
    ) {}

    private record ChatCompletionResponse(
            Choice[] choices
    ) {}

    private record Choice(
            Message message
    ) {}
}
