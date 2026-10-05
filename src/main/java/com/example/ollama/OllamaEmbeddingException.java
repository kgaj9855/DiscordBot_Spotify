package com.example.ollama;

public class OllamaEmbeddingException extends RuntimeException {

    private final Integer statusCode;

    public OllamaEmbeddingException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    private OllamaEmbeddingException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public static OllamaEmbeddingException forResponse(int statusCode, String apiMessage) {
        String message = switch (statusCode) {
            case 404 -> "Ollama embedding model or endpoint was not found (HTTP 404)";
            case 408, 504 -> "Ollama embedding request timed out (HTTP " + statusCode + ")";
            default -> "Ollama embedding request failed (HTTP " + statusCode + ")";
        };

        String safeMessage = sanitize(apiMessage);
        if (!safeMessage.isBlank()) {
            message += ": " + safeMessage;
        }
        return new OllamaEmbeddingException(statusCode, message);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    private static String sanitize(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = message.replaceAll("[\\r\\n]+", " ").trim();
        return sanitized.length() <= 300 ? sanitized : sanitized.substring(0, 300);
    }
}
