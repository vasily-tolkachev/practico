package com.myproject.practico.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreBoundaryTest {

    @Test
    void coreShouldNotContainAuthProviderOrSessionLogic() throws IOException {
        Path root = Path.of("src/main/java");
        List<String> forbiddenTokens = List.of(
                "TelegramAuthenticationProvider",
                "GoogleAuthenticationProvider",
                "RefreshSession",
                "/api/auth/login",
                "/api/auth/refresh",
                "/api/auth/sessions"
        );

        try (Stream<Path> files = Files.walk(root)) {
            List<String> violations = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> {
                        try {
                            String content = Files.readString(path);
                            return forbiddenTokens.stream()
                                    .filter(content::contains)
                                    .map(token -> path + ": " + token);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            assertTrue(violations.isEmpty(), "Forbidden auth tokens in core:\n" + String.join("\n", violations));
        }
    }
}
