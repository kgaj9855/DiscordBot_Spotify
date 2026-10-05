package com.example.ollama;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.example.ollama.DTO.OllamaEmbeddingRequest;
import com.example.ollama.DTO.OllamaEmbeddingResponse;

import reactor.core.publisher.Mono;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface OllamaEmbeddingClient {

    @PostExchange(value = "/api/embed", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<OllamaEmbeddingResponse> embed(@RequestBody OllamaEmbeddingRequest request);
}
