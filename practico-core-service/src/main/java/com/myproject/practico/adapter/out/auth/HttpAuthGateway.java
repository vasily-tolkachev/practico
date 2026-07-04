package com.myproject.practico.adapter.out.auth;

import com.myproject.practico.auth.AuthGateway;
import com.myproject.practico.auth.dto.LoginCommand;
import com.myproject.practico.auth.dto.TokenPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Profile("http-auth")
public class HttpAuthGateway implements AuthGateway {

    private final RestClient restClient;

    public HttpAuthGateway(@Value("${auth.service.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public TokenPair login(LoginCommand command) {
        TokenPairResponse response = restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "provider", command.provider().name(),
                        "providerToken", command.providerToken()
                ))
                .retrieve()
                .body(TokenPairResponse.class);
        if (response == null) {
            throw new IllegalStateException("Auth service returned empty login response");
        }
        return new TokenPair(response.accessToken(), response.refreshToken());
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        TokenPairResponse response = restClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("refreshToken", refreshToken))
                .retrieve()
                .body(TokenPairResponse.class);
        if (response == null) {
            throw new IllegalStateException("Auth service returned empty refresh response");
        }
        return new TokenPair(response.accessToken(), response.refreshToken());
    }

    private record TokenPairResponse(
            String accessToken,
            String refreshToken
    ) {
    }
}
