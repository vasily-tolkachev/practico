package com.myproject.practico.adapter.out.openai;

import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.port.out.QuickCheckPort;
import com.myproject.practico.application.service.EvaluationRequest;
import com.myproject.practico.application.service.EvaluationResult;
import com.myproject.practico.application.service.QuickCheckRequest;
import com.myproject.practico.application.service.QuickCheckResult;
import com.myproject.practico.config.OpenAiProperties;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiClient implements EvaluationPort, QuickCheckPort {

    private static final String PROMPT = """
            You are a technical learning coach for beginners.
            The user is learning this concept for the first time.
            Do not grade as an interview.
            Reward partial understanding.
            If the user identified the core idea, treat it as a good learning answer.
            The goal is learning progress, not filtering candidates.
            Evaluate in any language. Ignore typos.
            Evaluate ONLY the knowledge required to answer the asked question.
            Do not require information that was not requested.
            Do not reward extra information.
            Do not penalize missing extra details if the question is correctly answered.

            Use this score meaning:
            0-2: does not understand yet
            3-5: has some idea
            6-7: core idea understood
            8-10: excellent explanation

            Return JSON only in this shape:

            {
              "score": number from 0 to 10,
              "answeredQuestion": true or false,
              "evaluation": "short constructive evaluation",
              "learningCard": {
                "title": "short title",
                "explanation": "very short explanation of what was missing"
              },
              "quickCheck": {
                "question": "ONE tiny check question",
                "expectedAnswer": "short expected answer"
              }
            }

            If answeredQuestion is true, set "learningCard" and "quickCheck" to null.
            Keep "evaluation" short and supportive.
            Keep learningCard explanation under 70 words.
            LearningCard must focus only on what was missing in the user's answer.
            For quickCheck question:
            - ask exactly one tiny question
            - prefer true/false, choose one, or one short "why"
            - do not ask for long explanations or multiple parts

            Question:
            %s

            Answer:
            %s
            """;
    private static final String QUICK_CHECK_PROMPT = """
            You are checking a quick understanding question.

            Question:
            %s

            Expected answer:
            %s

            User answer:
            %s

            Determine whether the user's answer demonstrates understanding.

            The wording does NOT need to match.
            Accept synonyms and equivalent explanations.

            Return JSON only in this shape:
            {
              "correct": true,
              "feedback": "short constructive feedback"
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
    private static final Pattern QUICK_CHECK_CORRECT_PATTERN = Pattern.compile("\"correct\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUICK_CHECK_FEEDBACK_PATTERN = Pattern.compile("\"feedback\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private final OpenAiProperties properties;

    @Override
    public EvaluationResult evaluate(EvaluationRequest request) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI evaluation skipped: OPENAI_API_KEY is empty");
            return fallback("AI evaluation is unavailable: OPENAI_API_KEY is not configured.");
        }

        String content = callModel("evaluation", PROMPT.formatted(request.question(), request.answer()));
        if (content == null || content.isBlank()) {
            return fallback("AI evaluation is temporarily unavailable. Please try again.");
        }

        return parseResult(content);
    }

    @Override
    public QuickCheckResult evaluate(QuickCheckRequest request) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI quick check skipped: OPENAI_API_KEY is empty");
            return quickCheckFallback("Quick check is temporarily unavailable.");
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
            return quickCheckFallback("Quick check is temporarily unavailable.");
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
                                    new Message("system", "You are a concise and strict evaluator."),
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
        if (answeredQuestion) {
            quickCheck = null;
        }

        return new EvaluationResult(score, answeredQuestion, evaluation, learningCard, quickCheck);
    }

    private int parseScore(String content) {
        Matcher matcher = SCORE_PATTERN.matcher(content);
        if (!matcher.find()) {
            return 0;
        }
        return clampScore(Integer.parseInt(matcher.group(1)));
    }

    private String parseEvaluation(String content) {
        Matcher matcher = EVALUATION_PATTERN.matcher(content);
        if (!matcher.find()) {
            return "No evaluation provided.";
        }
        String value = unescape(matcher.group(1)).trim();
        return value.isEmpty() ? "No evaluation provided." : value;
    }

    private boolean parseAnsweredQuestion(String content, int score) {
        Matcher matcher = ANSWERED_QUESTION_PATTERN.matcher(content);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1).toLowerCase());
        }
        return score >= 6;
    }

    private LearningCard parseLearningCard(String content, boolean answeredQuestion, String evaluation) {
        Matcher textMatcher = LEARNING_CARD_TEXT_PATTERN.matcher(content);
        if (textMatcher.find()) {
            String explanation = unescape(textMatcher.group(1)).trim();
            if (!explanation.isEmpty()) {
                return new LearningCard("Key concept", explanation);
            }
        }

        Matcher objectMatcher = LEARNING_CARD_OBJECT_PATTERN.matcher(content);
        if (objectMatcher.find()) {
            String objectContent = objectMatcher.group(1);
            String title = parseField(objectContent, TITLE_PATTERN, "Key concept");
            String explanation = parseField(objectContent, EXPLANATION_PATTERN, "");
            if (!explanation.isBlank()) {
                return new LearningCard(title, explanation);
            }
        }

        if (!answeredQuestion) {
            return new LearningCard("Key concept", evaluation);
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

    private QuickCheckResult parseQuickCheckResult(String content) {
        Matcher correctMatcher = QUICK_CHECK_CORRECT_PATTERN.matcher(content);
        boolean correct = correctMatcher.find() && Boolean.parseBoolean(correctMatcher.group(1).toLowerCase());
        String feedback = parseField(content, QUICK_CHECK_FEEDBACK_PATTERN, correct ? "Correct." : "Almost. Try again.");
        return new QuickCheckResult(correct, feedback);
    }

    private String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private int clampScore(int rawScore) {
        return Math.max(0, Math.min(10, rawScore));
    }

    private QuickCheckResult quickCheckFallback(String feedback) {
        return new QuickCheckResult(false, feedback);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private EvaluationResult fallback(String evaluation) {
        return new EvaluationResult(
                0,
                false,
                evaluation,
                new LearningCard("Key concept", "Review the concept and try again."),
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
