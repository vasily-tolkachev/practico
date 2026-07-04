package com.myproject.practico.auth.adapter.out.provider.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleIdTokenVerifier {

    private final boolean enabled;
    private final String clientId;
    private final com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifier(
            @Value("${auth.providers.google.enabled:false}") boolean enabled,
            @Value("${auth.providers.google.client-id:}") String clientId
    ) {
        this.enabled = enabled;
        this.clientId = clientId;
        this.verifier = new Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(StringUtils.hasText(clientId) ? List.of(clientId) : List.of())
                .build();
    }

    public GooglePrincipal verify(String idToken) {
        if (!enabled) {
            throw new IllegalStateException("Google authentication provider is disabled");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("Google client id is not configured");
        }
        if (!StringUtils.hasText(idToken)) {
            throw new IllegalArgumentException("Google id token must not be empty");
        }
        try {
            GoogleIdToken parsed = verifier.verify(idToken.trim());
            if (parsed == null) {
                throw new IllegalArgumentException("Invalid Google id token");
            }
            GoogleIdToken.Payload payload = parsed.getPayload();
            String issuer = payload.getIssuer();
            if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                throw new IllegalArgumentException("Invalid Google id token issuer");
            }
            String subject = payload.getSubject();
            if (!StringUtils.hasText(subject)) {
                throw new IllegalArgumentException("Google id token subject is missing");
            }

            Object emailObj = payload.get("email");
            Object nameObj = payload.get("name");
            Object pictureObj = payload.get("picture");

            String email = emailObj instanceof String emailValue ? emailValue : null;
            String displayName = nameObj instanceof String nameValue && StringUtils.hasText(nameValue)
                    ? nameValue
                    : "Google User";
            String avatarUrl = pictureObj instanceof String pictureValue ? pictureValue : null;

            return new GooglePrincipal(subject, email, displayName, avatarUrl);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Failed to verify Google id token", e);
        }
    }
}
