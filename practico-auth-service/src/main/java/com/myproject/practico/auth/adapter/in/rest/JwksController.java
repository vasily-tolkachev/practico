package com.myproject.practico.auth.adapter.in.rest;

import com.myproject.practico.auth.application.port.GetJwksUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    private final GetJwksUseCase getJwksUseCase;

    public JwksController(GetJwksUseCase getJwksUseCase) {
        this.getJwksUseCase = getJwksUseCase;
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        return getJwksUseCase.getJwks();
    }
}
