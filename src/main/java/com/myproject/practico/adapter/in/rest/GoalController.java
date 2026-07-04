package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalProgramUseCase;
import com.myproject.practico.application.port.in.GetGoalResolutionStatusUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.application.port.in.StartLearningFromGoalUseCase;
import com.myproject.practico.application.goal.GoalLearningStartResult;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalResolutionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goals")
public class GoalController {

    private final CreateGoalUseCase createGoalUseCase;
    private final ListGoalsUseCase listGoalsUseCase;
    private final GetGoalUseCase getGoalUseCase;
    private final GetGoalProgramUseCase getGoalProgramUseCase;
    private final GetGoalResolutionStatusUseCase getGoalResolutionStatusUseCase;
    private final StartLearningFromGoalUseCase startLearningFromGoalUseCase;

    public GoalController(
            CreateGoalUseCase createGoalUseCase,
            ListGoalsUseCase listGoalsUseCase,
            GetGoalUseCase getGoalUseCase,
            GetGoalProgramUseCase getGoalProgramUseCase,
            GetGoalResolutionStatusUseCase getGoalResolutionStatusUseCase,
            StartLearningFromGoalUseCase startLearningFromGoalUseCase
    ) {
        this.createGoalUseCase = createGoalUseCase;
        this.listGoalsUseCase = listGoalsUseCase;
        this.getGoalUseCase = getGoalUseCase;
        this.getGoalProgramUseCase = getGoalProgramUseCase;
        this.getGoalResolutionStatusUseCase = getGoalResolutionStatusUseCase;
        this.startLearningFromGoalUseCase = startLearningFromGoalUseCase;
    }

    @PostMapping
    public ResponseEntity<Goal> create(@RequestBody CreateGoalRequest request) {
        if (request == null || isBlank(request.title())) {
            return ResponseEntity.badRequest().build();
        }
        String title = request.title().trim();
        String description = isBlank(request.description()) ? title : request.description().trim();
        return ResponseEntity.ok(createGoalUseCase.create(title, description));
    }

    @GetMapping
    public ResponseEntity<List<Goal>> list() {
        return ResponseEntity.ok(listGoalsUseCase.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> get(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getGoalUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/resolution-status")
    public ResponseEntity<GoalResolutionStatus> getResolutionStatus(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getGoalResolutionStatusUseCase.getByGoalId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/program")
    public ResponseEntity<LearningProgram> getGoalProgram(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getGoalProgramUseCase.getByGoalId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<GoalLearningStartResult> startFromGoal(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        String userId = authentication == null ? null : authentication.getName();
        if (isBlank(userId)) {
            return ResponseEntity.badRequest().build();
        }
        return startLearningFromGoalUseCase.start(id, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateGoalRequest(
            String title,
            String description
    ) {
    }

}
