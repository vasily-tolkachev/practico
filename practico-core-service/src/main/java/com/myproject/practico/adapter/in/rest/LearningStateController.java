package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;
import com.myproject.practico.application.service.PracticeAnswer;
import com.myproject.practico.auth.CurrentUserProvider;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning")
public class LearningStateController {

    private final GetLearningStateUseCase getLearningStateUseCase;
    private final StartLearningUseCase startLearningUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final ContinueLearningUseCase continueLearningUseCase;
    private final SubmitPracticeUseCase submitPracticeUseCase;
    private final SubmitQuickCheckUseCase submitQuickCheckUseCase;
    private final SubmitRetryUseCase submitRetryUseCase;
    private final CurrentUserProvider currentUserProvider;

    public LearningStateController(
            GetLearningStateUseCase getLearningStateUseCase,
            StartLearningUseCase startLearningUseCase,
            SubmitAnswerUseCase submitAnswerUseCase,
            ContinueLearningUseCase continueLearningUseCase,
            SubmitPracticeUseCase submitPracticeUseCase,
            SubmitQuickCheckUseCase submitQuickCheckUseCase,
            SubmitRetryUseCase submitRetryUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.getLearningStateUseCase = getLearningStateUseCase;
        this.startLearningUseCase = startLearningUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.continueLearningUseCase = continueLearningUseCase;
        this.submitPracticeUseCase = submitPracticeUseCase;
        this.submitQuickCheckUseCase = submitQuickCheckUseCase;
        this.submitRetryUseCase = submitRetryUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Current learning state", description = "Returns canonical LearningState.")
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
    public ResponseEntity<LearningState> state() {
        String userId = userId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(getLearningStateUseCase.getState(userId));
    }

    @PostMapping("/start")
    public ResponseEntity<LearningState> start() {
        String userId = userId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(startLearningUseCase.start(userId));
    }

    @PostMapping("/answer")
    public ResponseEntity<LearningState> answer(@RequestBody AnswerRequest request) {
        String userId = userId();
        if (request == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(submitAnswerUseCase.submit(userId, request.answer()));
    }

    @PostMapping("/continue")
    public ResponseEntity<LearningState> continueLearning() {
        String userId = userId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(continueLearningUseCase.continueLearning(userId));
    }

    @PostMapping("/practice")
    public ResponseEntity<LearningState> practice(@RequestBody PracticeRequest request) {
        String userId = userId();
        if (request == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        PracticeAnswer answer = new PracticeAnswer(
                request.booleanAnswer(),
                request.selectedOptions(),
                request.orderedOptions(),
                request.matches()
        );
        return ResponseEntity.ok(submitPracticeUseCase.submitPractice(userId, answer));
    }

    @PostMapping("/quick-check")
    public ResponseEntity<LearningState> quickCheck(@RequestBody PracticeRequest request) {
        String userId = userId();
        if (request == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        PracticeAnswer answer = new PracticeAnswer(
                request.booleanAnswer(),
                request.selectedOptions(),
                request.orderedOptions(),
                request.matches()
        );
        return ResponseEntity.ok(submitQuickCheckUseCase.submitQuickCheck(userId, answer));
    }

    @PostMapping("/retry")
    public ResponseEntity<LearningState> retry(@RequestBody PracticeRequest request) {
        String userId = userId();
        if (request == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        PracticeAnswer answer = new PracticeAnswer(
                request.booleanAnswer(),
                request.selectedOptions(),
                request.orderedOptions(),
                request.matches()
        );
        return ResponseEntity.ok(submitRetryUseCase.submitRetry(userId, answer));
    }

    public record AnswerRequest(
            String answer
    ) {
    }

    public record PracticeRequest(
            Boolean booleanAnswer,
            Set<Integer> selectedOptions,
            List<Integer> orderedOptions,
            Map<Integer, Integer> matches
    ) {
    }

    private String userId() {
        return currentUserProvider.currentUserId().map(Object::toString).orElse(null);
    }
}
