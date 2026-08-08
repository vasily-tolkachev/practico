package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.in.GetCurrentProgramUseCase;
import com.myproject.practico.application.port.in.GetGenerationMetricsUseCase;
import com.myproject.practico.application.port.in.GenerateMicroConceptContentUseCase;
import com.myproject.practico.application.port.in.GetMicroConceptGeneratedContentUseCase;
import com.myproject.practico.application.port.in.GetMicroConceptGenerationStatusUseCase;
import com.myproject.practico.application.port.in.GetProgramByIdUseCase;
import com.myproject.practico.application.port.in.GetProgramStatusUseCase;
import com.myproject.practico.application.port.in.GetProgramTreeUseCase;
import com.myproject.practico.application.port.in.StartLearningFromMicroConceptUseCase;
import com.myproject.practico.application.microconcept.MicroConceptGeneratedContentResult;
import com.myproject.practico.application.microconcept.MicroConceptGenerationStatusResult;
import com.myproject.practico.application.microconcept.MicroConceptGenerationTriggerResult;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramGenerationStatus;
import com.myproject.practico.application.program.GenerationStageMetrics;
import com.myproject.practico.auth.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/programs", "/programs"})
public class LearningProgramController {

    private final GetCurrentProgramUseCase getCurrentProgramUseCase;
    private final GetProgramByIdUseCase getProgramByIdUseCase;
    private final GetProgramTreeUseCase getProgramTreeUseCase;
    private final GetProgramStatusUseCase getProgramStatusUseCase;
    private final GetGenerationMetricsUseCase getGenerationMetricsUseCase;
    private final GenerateMicroConceptContentUseCase generateMicroConceptContentUseCase;
    private final GetMicroConceptGeneratedContentUseCase getMicroConceptGeneratedContentUseCase;
    private final GetMicroConceptGenerationStatusUseCase getMicroConceptGenerationStatusUseCase;
    private final StartLearningFromMicroConceptUseCase startLearningFromMicroConceptUseCase;
    private final CurrentUserProvider currentUserProvider;

    public LearningProgramController(
            GetCurrentProgramUseCase getCurrentProgramUseCase,
            GetProgramByIdUseCase getProgramByIdUseCase,
            GetProgramTreeUseCase getProgramTreeUseCase,
            GetProgramStatusUseCase getProgramStatusUseCase,
            GetGenerationMetricsUseCase getGenerationMetricsUseCase,
            GenerateMicroConceptContentUseCase generateMicroConceptContentUseCase,
            GetMicroConceptGeneratedContentUseCase getMicroConceptGeneratedContentUseCase,
            GetMicroConceptGenerationStatusUseCase getMicroConceptGenerationStatusUseCase,
            StartLearningFromMicroConceptUseCase startLearningFromMicroConceptUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.getCurrentProgramUseCase = getCurrentProgramUseCase;
        this.getProgramByIdUseCase = getProgramByIdUseCase;
        this.getProgramTreeUseCase = getProgramTreeUseCase;
        this.getProgramStatusUseCase = getProgramStatusUseCase;
        this.getGenerationMetricsUseCase = getGenerationMetricsUseCase;
        this.generateMicroConceptContentUseCase = generateMicroConceptContentUseCase;
        this.getMicroConceptGeneratedContentUseCase = getMicroConceptGeneratedContentUseCase;
        this.getMicroConceptGenerationStatusUseCase = getMicroConceptGenerationStatusUseCase;
        this.startLearningFromMicroConceptUseCase = startLearningFromMicroConceptUseCase;
        this.currentUserProvider = currentUserProvider;
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
    public ResponseEntity<LearningProgram> current() {
        if (currentUserProvider.currentUserId().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(getCurrentProgramUseCase.getCurrentProgram(currentUserProvider.currentUserId().get().toString()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.myproject.practico.domain.LearningProgram> getById(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getProgramByIdUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<LearningProgram> getTree(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getProgramTreeUseCase.getTree(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ProgramGenerationStatus> getStatus(@PathVariable("id") Long id) {
        if (!isValidId(id)) {
            return ResponseEntity.badRequest().build();
        }
        return getProgramStatusUseCase.getStatus(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/generation-metrics")
    public ResponseEntity<java.util.List<GenerationStageMetrics>> generationMetrics() {
        return ResponseEntity.ok(getGenerationMetricsUseCase.getMetrics());
    }

    @PostMapping("/{id}/micro-concepts/{microConceptId}/generate")
    public ResponseEntity<MicroConceptGenerationTriggerResult> generateMicroConceptContent(
            @PathVariable("id") Long programId,
            @PathVariable("microConceptId") Long microConceptId
    ) {
        if (!isValidId(programId) || !isValidId(microConceptId)) {
            return ResponseEntity.badRequest().build();
        }
        String userId = currentUserProvider.currentUserId().map(Object::toString).orElse(null);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return generateMicroConceptContentUseCase.generate(programId, microConceptId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/micro-concepts/{microConceptId}/generation-status")
    public ResponseEntity<MicroConceptGenerationStatusResult> getMicroConceptGenerationStatus(
            @PathVariable("id") Long programId,
            @PathVariable("microConceptId") Long microConceptId
    ) {
        if (!isValidId(programId) || !isValidId(microConceptId)) {
            return ResponseEntity.badRequest().build();
        }
        if (currentUserProvider.currentUserId().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return getMicroConceptGenerationStatusUseCase.getStatus(programId, microConceptId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/micro-concepts/{microConceptId}/content")
    public ResponseEntity<MicroConceptGeneratedContentResult> getMicroConceptGeneratedContent(
            @PathVariable("id") Long programId,
            @PathVariable("microConceptId") Long microConceptId
    ) {
        if (!isValidId(programId) || !isValidId(microConceptId)) {
            return ResponseEntity.badRequest().build();
        }
        if (currentUserProvider.currentUserId().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return getMicroConceptGeneratedContentUseCase.getContent(programId, microConceptId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/micro-concepts/{microConceptId}/start")
    public ResponseEntity<com.myproject.practico.application.learning.state.LearningState> startFromMicroConcept(
            @PathVariable("id") Long programId,
            @PathVariable("microConceptId") Long microConceptId
    ) {
        if (!isValidId(programId) || !isValidId(microConceptId)) {
            return ResponseEntity.badRequest().build();
        }
        String userId = currentUserProvider.currentUserId().map(Object::toString).orElse(null);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return startLearningFromMicroConceptUseCase.start(programId, microConceptId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}
