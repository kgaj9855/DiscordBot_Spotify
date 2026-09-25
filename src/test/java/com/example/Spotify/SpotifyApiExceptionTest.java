package com.example.Spotify;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpotifyApiExceptionTest {

    @Test
    void providesActionableForbiddenMessageWithoutLeakingTokens() {
        SpotifyApiException exception = SpotifyApiException.forResponse(
                403,
                "missing scope access_token=secret-token\nBearer abc123",
                null);

        assertTrue(exception.getMessage().contains("OAuth scopes"));
        assertTrue(exception.getMessage().contains("access_token=[redacted]"));
        assertTrue(exception.getMessage().contains("Bearer [redacted]"));
        assertFalse(exception.getMessage().contains("secret-token"));
        assertFalse(exception.getMessage().contains("abc123"));
    }

    @Test
    void includesRetryAfterForRateLimit() {
        SpotifyApiException exception = SpotifyApiException.forResponse(429, "", "7");

        assertTrue(exception.getMessage().contains("retry after 7 seconds"));
    }
}
