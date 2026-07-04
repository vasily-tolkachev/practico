package com.myproject.practico.application.port.out;

import com.myproject.practico.application.auth.AuthenticatedIdentity;
import com.myproject.practico.domain.AuthenticationProviderType;

public interface AuthenticationProvider {
    AuthenticationProviderType type();
    AuthenticatedIdentity authenticate(String providerToken);
}
