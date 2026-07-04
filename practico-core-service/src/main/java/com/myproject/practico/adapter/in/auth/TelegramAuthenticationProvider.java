package com.myproject.practico.adapter.in.auth;

import com.myproject.practico.application.auth.AuthenticatedIdentity;
import com.myproject.practico.application.port.out.AuthenticationProvider;
import com.myproject.practico.config.TelegramProperties;
import com.myproject.practico.domain.AuthenticationProviderType;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class TelegramAuthenticationProvider implements AuthenticationProvider {

    private final TelegramProperties telegramProperties;

    public TelegramAuthenticationProvider(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;
    }

    @Override
    public AuthenticationProviderType type() {
        return AuthenticationProviderType.TELEGRAM;
    }

    @Override
    public AuthenticatedIdentity authenticate(String providerToken) {
        Map<String, String> data = parsePayload(providerToken);
        String hash = data.remove("hash");
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Telegram payload hash is missing");
        }

        String checkString = data.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
        String expectedHash = hmacSha256Hex(secretKey(), checkString);
        if (!MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Telegram payload hash validation failed");
        }

        String authDate = data.get("auth_date");
        if (authDate == null) {
            throw new IllegalArgumentException("Telegram payload auth_date is missing");
        }
        long authEpochSeconds = Long.parseLong(authDate);
        if (Instant.ofEpochSecond(authEpochSeconds).isBefore(Instant.now().minusSeconds(86400))) {
            throw new IllegalArgumentException("Telegram payload has expired");
        }

        String id = data.get("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Telegram payload id is missing");
        }

        String displayName = joinNonBlank(data.get("first_name"), data.get("last_name"), data.get("username"));
        String avatarUrl = data.get("photo_url");
        return new AuthenticatedIdentity(
                AuthenticationProviderType.TELEGRAM,
                id,
                null,
                displayName == null || displayName.isBlank() ? id : displayName,
                avatarUrl
        );
    }

    private Map<String, String> parsePayload(String providerToken) {
        if (providerToken == null || providerToken.isBlank()) {
            throw new IllegalArgumentException("Telegram payload is empty");
        }
        Map<String, String> sorted = new TreeMap<>();
        for (String part : providerToken.split("&")) {
            String[] tokenPair = part.split("=", 2);
            if (tokenPair.length != 2) {
                continue;
            }
            String key = URLDecoder.decode(tokenPair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(tokenPair[1], StandardCharsets.UTF_8);
            sorted.put(key, value);
        }
        return sorted;
    }

    private byte[] secretKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(telegramProperties.botToken().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate Telegram secret key", ex);
        }
    }

    private String hmacSha256Hex(byte[] key, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to validate Telegram payload", ex);
        }
    }

    private String joinNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "));
    }
}
