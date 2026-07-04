package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.application.dto.AuthenticatedIdentity;
import com.myproject.practico.auth.domain.AuthenticationProviderType;

public interface AuthenticationProvider {
    AuthenticationProviderType providerType();
    AuthenticatedIdentity authenticate(String providerToken);
}
