package com.myproject.practico.adapter.out.openai;

import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.service.EvaluationRequest;
import com.myproject.practico.application.service.EvaluationResult;
import com.myproject.practico.config.OpenAiProperties;
import com.myproject.practico.domain.LearningCard;
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
public class OpenAiClient implements EvaluationPort {

    private static final String PROMPT = """
            You are a technical learning coach.

            Evaluate the candidate answer in any language. Ignore typos.

            Return JSON only in this shape:

            {
              "score": number from 0 to 10,
              "evaluation": "short constructive evaluation",
              "learningCard": {
                "title": "short title",
                "explanation": "clear explanation of what to learn next"
              }
            }

            If score is 8 or higher, set "learningCard" to null.

            Question:
            %s

            Answer:
            %s
            """;
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
    private static final Pattern EVALUATION_PATTERN = Pattern.compile("\"evaluation\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern LEARNING_CARD_TEXT_PATTERN =
            Pattern.compile("\"learningCard\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern LEARNING_CARD_OBJECT_PATTERN =
            Pattern.compile("\"learningCard\"\\s*:\\s*\\{([\\s\\S]*?)\\}");
    private static final Pattern TITLE_PATTERN = Pattern.compile("\"title\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern EXPLANATION_PATTERN = Pattern.compile("\"explanation\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private final OpenAiProperties properties;

    @Override
    public EvaluationResult evaluate(EvaluationRequest request) {
        String model = properties.model() == null || properties.model().isBlank()
                ? "gpt-4.1-mini"
                : properties.model();
        String apiKey = properties.apiKey();

        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI evaluation skipped: OPENAI_API_KEY is empty");
            return fallback("AI evaluation is unavailable: OPENAI_API_KEY is not configured.");
        }

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
                                    new Message("user", PROMPT.formatted(request.question(), request.answer()))
                            },
                            new ResponseFormat("json_object")
                    ))
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().length == 0) {
                log.error("OpenAI evaluation failed: empty response. model={}", model);
                return fallback("AI evaluation is temporarily unavailable. Empty response from model.");
            }

            return parseResult(response.choices()[0].message().content());
        } catch (RestClientResponseException ex) {
            log.error(
                    "OpenAI evaluation HTTP error. model={}, status={}, body={}",
                    model,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
            return fallback("AI evaluation is temporarily unavailable. Check OPENAI_API_KEY / OPENAI_MODEL.");
        } catch (Exception ex) {
            log.error("OpenAI evaluation failed. model={}", model, ex);
            return fallback("AI evaluation is temporarily unavailable. Please try again.");
        }
    }

    private EvaluationResult parseResult(String content) {
        int score = parseScore(content);
        String evaluation = parseEvaluation(content);
        LearningCard learningCard = parseLearningCard(content, score, evaluation);

        return new EvaluationResult(score, evaluation, learningCard);
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

    private LearningCard parseLearningCard(String content, int score, String evaluation) {
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

        if (score < 8) {
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

    private String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private int clampScore(int rawScore) {
        return Math.max(0, Math.min(10, rawScore));
    }

    private EvaluationResult fallback(String evaluation) {
        return new EvaluationResult(
                0,
                evaluation,
                new LearningCard("Key concept", "Review the concept and try again.")
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
