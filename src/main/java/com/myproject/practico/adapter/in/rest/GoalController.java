package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalResolutionStatusUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalResolutionStatus;
import org.springframework.http.ResponseEntity;
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
    private final GetGoalResolutionStatusUseCase getGoalResolutionStatusUseCase;

    public GoalController(
            CreateGoalUseCase createGoalUseCase,
            ListGoalsUseCase listGoalsUseCase,
            GetGoalUseCase getGoalUseCase,
            GetGoalResolutionStatusUseCase getGoalResolutionStatusUseCase
    ) {
        this.createGoalUseCase = createGoalUseCase;
        this.listGoalsUseCase = listGoalsUseCase;
        this.getGoalUseCase = getGoalUseCase;
        this.getGoalResolutionStatusUseCase = getGoalResolutionStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<Goal> create(@RequestBody CreateGoalRequest request) {
        if (request == null || isBlank(request.title())) {
            return ResponseEntity.badRequest().build();
        }
        String description = isBlank(request.description()) ? request.title() : request.description();
        return ResponseEntity.ok(createGoalUseCase.create(request.title().trim(), description.trim()));
    }

    @GetMapping
    public ResponseEntity<List<Goal>> list() {
        return ResponseEntity.ok(listGoalsUseCase.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> get(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return getGoalUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/resolution-status")
    public ResponseEntity<GoalResolutionStatus> getResolutionStatus(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return getGoalResolutionStatusUseCase.getByGoalId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
