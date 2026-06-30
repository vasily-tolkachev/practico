package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.in.GetCurrentProgramUseCase;
import com.myproject.practico.application.program.LearningProgram;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Current learning program", description = "Returns curriculum structure for the current user context.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Learning program payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearningProgram.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid userId")
    })
    @GetMapping("/current")
    public ResponseEntity<LearningProgram> current(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(getCurrentProgramUseCase.getCurrentProgram(userId));
    }
}
