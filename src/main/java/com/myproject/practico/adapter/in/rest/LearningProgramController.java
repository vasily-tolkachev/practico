package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.in.GetCurrentProgramUseCase;
import com.myproject.practico.application.program.LearningProgram;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/programs")
public class LearningProgramController {

    private final GetCurrentProgramUseCase getCurrentProgramUseCase;

    public LearningProgramController(GetCurrentProgramUseCase getCurrentProgramUseCase) {
        this.getCurrentProgramUseCase = getCurrentProgramUseCase;
    }

    @GetMapping("/current")
    public ResponseEntity<LearningProgram> current(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(getCurrentProgramUseCase.getCurrentProgram(userId));
    }
}
