package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.adapter.in.rest.dto.LearningStateResponse;
import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.service.LearningCycle;
import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.LearningSessionService;
import com.myproject.practico.application.service.LearningSessionStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningStateController {

    private final LearningSessionService learningSessionService;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final LearningStateMapper learningStateMapper;
    private final GetLearningStateUseCase getLearningStateUseCase;

    public LearningStateController(
            LearningSessionService learningSessionService,
            SubmitAnswerUseCase submitAnswerUseCase,
            LearningStateMapper learningStateMapper,
            GetLearningStateUseCase getLearningStateUseCase
    ) {
        this.learningSessionService = learningSessionService;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.learningStateMapper = learningStateMapper;
        this.getLearningStateUseCase = getLearningStateUseCase;
    }

    @Operation(summary = "Current learning state", description = "Returns LearningState v1 for the user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Learning state payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LearningState.class),
                            examples = {
                                    @ExampleObject(name = "QUESTION", externalValue = "/openapi-examples/learning-state-question.json"),
                                    @ExampleObject(name = "LEARNING_CARD", externalValue = "/openapi-examples/learning-state-learning-card.json"),
                                    @ExampleObject(name = "PRACTICE", externalValue = "/openapi-examples/learning-state-practice.json"),
                                    @ExampleObject(name = "RETRY", externalValue = "/openapi-examples/learning-state-retry.json"),
                                    @ExampleObject(name = "COMPLETED", externalValue = "/openapi-examples/learning-state-completed.json")
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid userId")
    })
    @GetMapping("/state")
    public ResponseEntity<LearningState> state(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(getLearningStateUseCase.getState(userId));
    }

    @Operation(summary = "Submit answer", description = "Accepts user answer and returns updated LearningState v1.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated learning state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearningStateResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/answer")
    public ResponseEntity<LearningStateResponse> answer(@RequestBody AnswerRequest request) {
        if (request == null || request.userId() == null || request.userId().isBlank()
                || request.answer() == null || request.answer().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        submitAnswerUseCase.submit(request.userId(), request.answer());
        return ResponseEntity.ok(learningStateMapper.toState(request.userId()));
    }

    @Operation(summary = "Continue learning", description = "Advances flow after learning card (LEARNING_CARD -> PRACTICE/QUICK_CHECK/RETRY).")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated learning state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearningStateResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/continue")
    public ResponseEntity<LearningStateResponse> continueLearning(@RequestBody ContinueRequest request) {
        if (request == null || request.userId() == null || request.userId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        LearningSessionStore.LearningSession session = learningSessionService.getSession(request.userId()).orElse(null);
        if (session == null) {
            return ResponseEntity.ok(learningStateMapper.inactiveState(request.userId()));
        }

        if (session.phase() == LearningPhase.LEARNING_CARD) {
            LearningCycle cycle = session.currentCycle();
            List<?> practiceItems = cycle == null || cycle.practiceItems() == null ? List.of() : cycle.practiceItems();
            if (!practiceItems.isEmpty()) {
                learningSessionService.setPracticeIndex(request.userId(), 0);
                learningSessionService.setPhase(request.userId(), LearningPhase.PRACTICE);
            } else if (cycle != null
                    && cycle.quickCheck() != null
                    && cycle.quickCheck().question() != null
                    && !cycle.quickCheck().question().isBlank()) {
                learningSessionService.setPhase(request.userId(), LearningPhase.QUICK_CHECK);
            } else {
                learningSessionService.setPhase(request.userId(), LearningPhase.RETRY);
            }
        }

        return ResponseEntity.ok(learningStateMapper.toState(request.userId()));
    }

    public record AnswerRequest(
            String userId,
            String answer
    ) {}

    public record ContinueRequest(
            String userId
    ) {}
}
