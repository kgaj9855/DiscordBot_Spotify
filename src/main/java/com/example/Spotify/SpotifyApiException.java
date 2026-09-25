package com.example.Spotify;

public class SpotifyApiException extends RuntimeException {

    private final Integer statusCode;

    public SpotifyApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    private SpotifyApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public static SpotifyApiException forResponse(int statusCode, String apiMessage, String retryAfter) {
        String message = switch (statusCode) {
            case 401 -> "Spotify API authorization failed (HTTP 401)";
            case 403 -> "Spotify API access forbidden (HTTP 403); verify required OAuth scopes";
            case 429 -> "Spotify API rate limit exceeded (HTTP 429)"
                    + (retryAfter == null || retryAfter.isBlank() ? "" : "; retry after " + retryAfter + " seconds");
            default -> "Spotify API request failed (HTTP " + statusCode + ")";
        };

        String safeApiMessage = sanitize(apiMessage);
        if (!safeApiMessage.isBlank()) {
            message += ": " + safeApiMessage;
        }
        return new SpotifyApiException(statusCode, message);
    }

    public boolean isUnauthorized() {
        return Integer.valueOf(401).equals(statusCode);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    private static String sanitize(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = message
                .replaceAll("(?i)Bearer\\s+[^\\s,;]+", "Bearer [redacted]")
                .replaceAll("(?i)(access_token|refresh_token|client_secret)\\s*[:=]\\s*[^\\s,;]+", "$1=[redacted]")
                .replaceAll("[\\r\\n]+", " ")
                .trim();
        return sanitized.length() <= 300 ? sanitized : sanitized.substring(0, 300);
    }
}
