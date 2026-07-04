package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.adapter.out.security.JwksProviderAdapter;
import com.myproject.practico.auth.application.port.GetJwksUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GetJwksService implements GetJwksUseCase {

    private final JwksProviderAdapter jwksProviderAdapter;

    public GetJwksService(JwksProviderAdapter jwksProviderAdapter) {
        this.jwksProviderAdapter = jwksProviderAdapter;
    }

    @Override
    public Map<String, Object> getJwks() {
        return jwksProviderAdapter.jwks();
    }
}
