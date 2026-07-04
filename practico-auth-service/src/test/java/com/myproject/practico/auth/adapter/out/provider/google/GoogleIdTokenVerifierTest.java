package com.myproject.practico.auth.adapter.out.provider.google;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleIdTokenVerifierTest {

    @Test
    void shouldRejectWhenDisabled() {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(false);
        assertThrows(IllegalStateException.class, () -> verifier.verify("token"));
    }

    @Test
    void shouldVerifyWhenEnabled() {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(true);
        GooglePrincipal principal = verifier.verify("sub-123");
        assertEquals("sub-123", principal.subject());
    }
}
