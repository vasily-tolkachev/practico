package com.myproject.practico.auth.adapter.out.security;

import com.myproject.practico.auth.config.JwtKeyConfig;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwksProviderAdapter {

    private final JwtKeyConfig jwtKeyConfig;
    private final JwtKeyMaterial keyMaterial;

    public JwksProviderAdapter(JwtKeyConfig jwtKeyConfig, JwtKeyMaterial keyMaterial) {
        this.jwtKeyConfig = jwtKeyConfig;
        this.keyMaterial = keyMaterial;
    }

    public Map<String, Object> jwks() {
        RSAPublicKey publicKey = keyMaterial.publicKey();
        Map<String, Object> key = Map.of(
                "kty", "RSA",
                "kid", jwtKeyConfig.keyId(),
                "use", "sig",
                "alg", "RS256",
                "n", toBase64Url(publicKey.getModulus()),
                "e", toBase64Url(publicKey.getPublicExponent())
        );
        return Map.of("keys", List.of(key));
    }

    private String toBase64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int offset = bytes.length > 1 && bytes[0] == 0 ? 1 : 0;
        byte[] normalized = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, normalized, 0, normalized.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(normalized);
    }
}
