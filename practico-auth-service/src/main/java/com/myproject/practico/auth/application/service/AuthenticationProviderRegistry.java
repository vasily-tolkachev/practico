package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.port.AuthenticationProvider;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationProviderRegistry {

    private final Map<AuthenticationProviderType, AuthenticationProvider> providers;

    public AuthenticationProviderRegistry(List<AuthenticationProvider> providers) {
        Map<AuthenticationProviderType, AuthenticationProvider> map = new EnumMap<>(AuthenticationProviderType.class);
        for (AuthenticationProvider provider : providers) {
            map.put(provider.providerType(), provider);
        }
        this.providers = map;
    }

    public AuthenticationProvider get(AuthenticationProviderType type) {
        AuthenticationProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported provider: " + type);
        }
        return provider;
    }
}
