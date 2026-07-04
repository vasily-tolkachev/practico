package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.AuthenticationProvider;
import com.myproject.practico.domain.AuthenticationProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AuthenticationProviderRegistry {

    private final Map<AuthenticationProviderType, AuthenticationProvider> providers;

    public AuthenticationProviderRegistry(java.util.List<AuthenticationProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(AuthenticationProvider::type, Function.identity()));
    }

    public AuthenticationProvider resolve(AuthenticationProviderType providerType) {
        AuthenticationProvider provider = providers.get(providerType);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerType);
        }
        return provider;
    }
}
