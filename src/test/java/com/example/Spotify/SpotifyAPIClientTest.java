package com.example.Spotify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class SpotifyAPIClientTest {

    private HttpServer server;
    private SpotifyAPIClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        client = new SpotifyApiClientConfiguration().spotifyAPIClient(
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
    void sendsBearerHeaderAndLimitUsingDeclarativeClient() {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> query = new AtomicReference<>();
        server.createContext("/me/player/recently-played", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            query.set(exchange.getRequestURI().getQuery());
            respond(exchange, 200, "{\"limit\":20,\"items\":[]}");
        });

        var response = client.getRecentlyPlayed("Bearer test-token", 20).block();

        assertEquals("Bearer test-token", authorization.get());
        assertEquals("limit=20", query.get());
        assertEquals(20, response.limit());
        assertEquals(0, response.items().size());
    }

    @Test
    void translatesForbiddenResponseWithoutExposingAuthorizationHeader() {
        server.createContext("/me/player/recently-played", exchange -> respond(
                exchange,
                403,
                "{\"error\":{\"status\":403,\"message\":\"Missing user-read-recently-played scope\"}}"));

        SpotifyApiException exception = assertThrows(
                SpotifyApiException.class,
                () -> client.getRecentlyPlayed("Bearer secret-token", 20).block());

        assertEquals(403, exception.getStatusCode());
        assertFalse(exception.getMessage().contains("secret-token"));
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
