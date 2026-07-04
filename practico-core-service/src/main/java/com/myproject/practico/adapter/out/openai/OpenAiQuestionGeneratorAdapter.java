package com.myproject.practico.adapter.out.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
import com.myproject.practico.application.program.GeneratedQuestionBatch;
import com.myproject.practico.application.program.GeneratedQuestion;
import com.myproject.practico.config.OpenAiProperties;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiQuestionGeneratorAdapter implements AiQuestionGeneratorPort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PROMPT = """
            Generate 5-7 short learning questions for the micro-concept.
            Return JSON only:
            {
              "questions": [
                {
                  "text": "...",
                  "expectedAnswer": "...",
                  "explanation": "...",
                  "difficulty": "EASY|MEDIUM|HARD",
                  "questionType": "DEFINITION|UNDERSTANDING|APPLICATION|COMPARISON"
                }
              ]
            }
            goal: %s
            topic: %s
            concept: %s
            microConcept: %s
            """;

    private final OpenAiProperties properties;

    @Override
    public GeneratedQuestionBatch generateQuestions(String goalTitle, String topicName, String conceptName, String microConceptName) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return new GeneratedQuestionBatch(fallback(microConceptName), null, null);
        }
        String model = properties.model() == null || properties.model().isBlank() ? "gpt-5-mini" : properties.model();
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
                                    new Message("system", "Return strict JSON only."),
                                    new Message("user", PROMPT.formatted(safe(goalTitle), safe(topicName), safe(conceptName), safe(microConceptName)))
                            },
                            new ResponseFormat("json_object")
                    ))
                    .retrieve()
                    .body(ChatCompletionResponse.class);
            if (response == null || response.choices() == null || response.choices().length == 0) {
                return new GeneratedQuestionBatch(fallback(microConceptName), null, null);
            }
            String content = response.choices()[0].message().content();
            if (content == null || content.isBlank()) {
                return new GeneratedQuestionBatch(fallback(microConceptName), null, null);
            }
            return new GeneratedQuestionBatch(
                    parse(content, microConceptName),
                    response.usage() == null ? null : response.usage().total_tokens(),
                    null
            );
        } catch (Exception ex) {
            log.error("Question generation failed", ex);
            return new GeneratedQuestionBatch(fallback(microConceptName), null, null);
        }
    }

    private List<GeneratedQuestion> parse(String content, String microConceptName) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(content);
            JsonNode array = root.path("questions");
            if (!array.isArray()) {
                return fallback(microConceptName);
            }
            List<GeneratedQuestion> questions = new ArrayList<>();
            for (JsonNode node : array) {
                String text = safe(node.path("text").asText()).trim();
                if (text.isBlank()) {
                    continue;
                }
                String expectedAnswer = safe(node.path("expectedAnswer").asText()).trim();
                String explanation = safe(node.path("explanation").asText()).trim();
                Difficulty difficulty = parseDifficulty(node.path("difficulty").asText());
                QuestionType questionType = parseQuestionType(node.path("questionType").asText());
                questions.add(new GeneratedQuestion(text, expectedAnswer, explanation, difficulty, questionType));
            }
            return questions.isEmpty() ? fallback(microConceptName) : questions;
        } catch (Exception ex) {
            return fallback(microConceptName);
        }
    }

    private List<GeneratedQuestion> fallback(String microConceptName) {
        String label = safe(microConceptName).isBlank() ? "this concept" : microConceptName.trim();
        return List.of(
                new GeneratedQuestion(
                        "What is " + label + "?",
                        label + " is a key idea in this section.",
                        "This checks the basic definition and scope.",
                        Difficulty.EASY,
                        QuestionType.DEFINITION
                ),
                new GeneratedQuestion(
                        "When would you use " + label + " in practice?",
                        "Use it when the scenario matches its strengths and constraints.",
                        "This checks practical application.",
                        Difficulty.MEDIUM,
                        QuestionType.APPLICATION
                )
        );
    }

    private Difficulty parseDifficulty(String value) {
        try {
            return Difficulty.valueOf(safe(value).trim().toUpperCase());
        } catch (Exception ex) {
            return Difficulty.MEDIUM;
        }
    }

    private QuestionType parseQuestionType(String value) {
        try {
            return QuestionType.valueOf(safe(value).trim().toUpperCase());
        } catch (Exception ex) {
            return QuestionType.UNDERSTANDING;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record ChatCompletionRequest(
            String model,
            Message[] messages,
            ResponseFormat response_format
    ) {
    }

    private record Message(
            String role,
            String content
    ) {
    }

    private record ResponseFormat(
            String type
    ) {
    }

    private record ChatCompletionResponse(
            Choice[] choices,
            Usage usage
    ) {
    }

    private record Choice(
            Message message
    ) {
    }

    private record Usage(
            Long total_tokens
    ) {
    }
}
