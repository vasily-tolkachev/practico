package com.myproject.practico.adapter.out.openai;

`import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.service.EvaluationRequest;
import com.myproject.practico.application.service.EvaluationResult;
import com.myproject.practico.config.OpenAiProperties;
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

            Evaluate the candidate answer given in any language. Do not pay attention on typos.

            Return JSON only:

            {
              "score": number from 0 to 10,
              "feedback": "short constructive feedback"
            }

            Question:
            %s

            Answer:
            %s
            """;
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
    private static final Pattern FEEDBACK_PATTERN = Pattern.compile("\"feedback\"\\s*:\\s*\"([^\"]*)\"");

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

            String content = response.choices()[0].message().content();
            int score = parseScore(content);
            String feedback = parseFeedback(content);
            return new EvaluationResult(score, feedback);
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

    private int parseScore(String content) {
        Matcher matcher = SCORE_PATTERN.matcher(content);
        if (!matcher.find()) {
            return 0;
        }

        int rawScore = Integer.parseInt(matcher.group(1));
        return Math.max(0, Math.min(10, rawScore));
    }

    private String parseFeedback(String content) {
        Matcher matcher = FEEDBACK_PATTERN.matcher(content);
        if (!matcher.find()) {
            return "No feedback provided.";
        }

        String feedback = matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        return feedback.isBlank() ? "No feedback provided." : feedback;
    }

    private EvaluationResult fallback(String feedback) {
        return new EvaluationResult(
                0,
                feedback
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
