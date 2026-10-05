package com.example.ollama;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.ollama.DTO.OllamaEmbeddingRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class OllamaEmbeddingClientTest {

    private HttpServer server;
    private OllamaEmbeddingClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        client = new OllamaEmbeddingClientConfiguration().ollamaEmbeddingClient(
                WebClient.builder(),
                "http://127.0.0.1:" + server.getAddress().getPort(),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsEmbeddingRequestToDeclarativeEndpoint() {
        AtomicReference<String> requestBody = new AtomicReference<>();
        server.createContext("/api/embed", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, "{\"model\":\"nomic-embed-text:latest\",\"embeddings\":[[0.1,0.2]]}");
        });

        var response = client.embed(new OllamaEmbeddingRequest(
                "nomic-embed-text:latest", "search_query: romantic song")).block();

        assertEquals("nomic-embed-text:latest", response.model());
        assertEquals(2, response.embeddings().getFirst().size());
        assertTrue(requestBody.get().contains("\"model\":\"nomic-embed-text:latest\""));
        assertTrue(requestBody.get().contains("\"input\":\"search_query: romantic song\""));
    }

    @Test
    void translatesOllamaErrorWithoutLeakingMultilineResponse() {
        server.createContext("/api/embed", exchange -> respond(
                exchange,
                404,
                "{\"error\":\"model not found\\nplease pull it\"}"));

        OllamaEmbeddingException exception = assertThrows(
                OllamaEmbeddingException.class,
                () -> client.embed(new OllamaEmbeddingRequest("missing", "query")).block());

        assertEquals(404, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("model not found"));
        assertFalse(exception.getMessage().contains("\n"));
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
