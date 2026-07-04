package com.myproject.practico.auth.adapter.in.rest;

import com.myproject.practico.auth.adapter.in.rest.dto.RevokeRequest;
import com.myproject.practico.auth.application.dto.RevokeSessionRequest;
import com.myproject.practico.auth.application.port.RevokeSessionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/sessions")
public class SessionController {

    private final RevokeSessionUseCase revokeSessionUseCase;

    public SessionController(RevokeSessionUseCase revokeSessionUseCase) {
        this.revokeSessionUseCase = revokeSessionUseCase;
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RevokeRequest request) {
        revokeSessionUseCase.revoke(new RevokeSessionRequest(request.sessionId()));
        return ResponseEntity.noContent().build();
    }
}
