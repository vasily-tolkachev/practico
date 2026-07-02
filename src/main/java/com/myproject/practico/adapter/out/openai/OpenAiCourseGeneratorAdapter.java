package com.myproject.practico.adapter.out.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.port.out.AiCourseGeneratorPort;
import com.myproject.practico.application.program.GeneratedConceptStructure;
import com.myproject.practico.application.program.GeneratedProgramStructure;
import com.myproject.practico.application.program.GeneratedTopicStructure;
import com.myproject.practico.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiCourseGeneratorAdapter implements AiCourseGeneratorPort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SYSTEM_PROMPT = """
            You generate curriculum structures.
            Return only valid JSON.
            Keep names concise and practical.
            """;
    private static final String USER_PROMPT = """
            Build a learning program structure for this goal:
            title: %s
            description: %s

            Return JSON in this exact shape:
            {
              "topics": [
                {
                  "name": "Topic name",
                  "concepts": [
                    {
                      "name": "Concept name",
                      "microConcepts": ["Micro concept 1", "Micro concept 2"]
                    }
                  ]
                }
              ]
            }

            Constraints:
            - 3-6 topics
            - each topic 3-6 concepts
            - each concept 3-6 microConcepts
            - no questions, no explanations
            - avoid duplicates
            """;

    private final OpenAiProperties properties;

    @Override
    public GeneratedProgramStructure generateProgramStructure(String goalTitle, String goalDescription) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return fallback(goalTitle);
        }

        String model = properties.model() == null || properties.model().isBlank()
                ? "gpt-5-mini"
                : properties.model();
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
                                    new Message("system", SYSTEM_PROMPT),
                                    new Message("user", USER_PROMPT.formatted(safe(goalTitle), safe(goalDescription)))
                            },
                            new ResponseFormat("json_object")
                    ))
                    .retrieve()
                    .body(ChatCompletionResponse.class);
            if (response == null || response.choices() == null || response.choices().length == 0) {
                return fallback(goalTitle);
            }
            String content = response.choices()[0].message().content();
            if (content == null || content.isBlank()) {
                return fallback(goalTitle);
            }
            return parse(content, goalTitle);
        } catch (RestClientResponseException ex) {
            log.error("Program generation HTTP error status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return fallback(goalTitle);
        } catch (Exception ex) {
            log.error("Program generation failed", ex);
            return fallback(goalTitle);
        }
    }

    private GeneratedProgramStructure parse(String json, String goalTitle) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode topicsNode = root.path("topics");
            if (!topicsNode.isArray()) {
                return fallback(goalTitle);
            }

            List<GeneratedTopicStructure> topics = new ArrayList<>();
            for (JsonNode topicNode : topicsNode) {
                String topicName = safe(topicNode.path("name").asText()).trim();
                if (topicName.isBlank()) {
                    continue;
                }

                List<GeneratedConceptStructure> concepts = new ArrayList<>();
                JsonNode conceptsNode = topicNode.path("concepts");
                if (conceptsNode.isArray()) {
                    for (JsonNode conceptNode : conceptsNode) {
                        String conceptName = safe(conceptNode.path("name").asText()).trim();
                        if (conceptName.isBlank()) {
                            continue;
                        }

                        List<String> microConcepts = new ArrayList<>();
                        JsonNode microNode = conceptNode.path("microConcepts");
                        if (microNode.isArray()) {
                            for (JsonNode micro : microNode) {
                                String microName = safe(micro.asText()).trim();
                                if (!microName.isBlank()) {
                                    microConcepts.add(microName);
                                }
                            }
                        }
                        if (!microConcepts.isEmpty()) {
                            concepts.add(new GeneratedConceptStructure(conceptName, microConcepts));
                        }
                    }
                }

                if (!concepts.isEmpty()) {
                    topics.add(new GeneratedTopicStructure(topicName, concepts));
                }
            }

            return topics.isEmpty() ? fallback(goalTitle) : new GeneratedProgramStructure(topics);
        } catch (Exception ex) {
            log.error("Failed to parse generated program JSON", ex);
            return fallback(goalTitle);
        }
    }

    private GeneratedProgramStructure fallback(String goalTitle) {
        String safeGoal = safe(goalTitle).isBlank() ? "Goal" : goalTitle.trim();
        return new GeneratedProgramStructure(List.of(
                new GeneratedTopicStructure(
                        safeGoal + " Fundamentals",
                        List.of(
                                new GeneratedConceptStructure("Core Principles", List.of("Terminology", "Mental Model", "Common Pitfalls")),
                                new GeneratedConceptStructure("Basic Usage", List.of("Setup", "Simple Flow", "Debug Basics"))
                        )
                )
        ));
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
            Choice[] choices
    ) {
    }

    private record Choice(
            Message message
    ) {
    }
}
