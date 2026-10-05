package com.example.ollama;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ollama.DTO.OllamaEmbeddingRequest;
import com.example.ollama.DTO.OllamaEmbeddingResponse;

import reactor.core.publisher.Mono;

@Service
public class OllamaEmbeddingService {

    static final int EMBEDDING_DIMENSIONS = 768;
    static final String QUERY_PREFIX = "search_query: ";

    private final OllamaEmbeddingClient client;
    private final String embeddingModel;

    public OllamaEmbeddingService(
            OllamaEmbeddingClient client,
            @Value("${OLLAMA_EMBEDDING_MODEL}") String embeddingModel) {
        if (embeddingModel == null || embeddingModel.isBlank()) {
            throw new IllegalArgumentException("OLLAMA_EMBEDDING_MODEL must not be blank");
        }
        this.client = client;
        this.embeddingModel = embeddingModel;
    }

    public Mono<List<Double>> embedSemanticQuery(String semanticQuery) {
        if (semanticQuery == null || semanticQuery.isBlank()) {
            return Mono.error(new IllegalArgumentException("Semantic query must not be blank"));
        }

        OllamaEmbeddingRequest request = new OllamaEmbeddingRequest(
                embeddingModel,
                QUERY_PREFIX + semanticQuery.trim());

        return client.embed(request)
                .map(this::extractEmbedding);
    }

    private List<Double> extractEmbedding(OllamaEmbeddingResponse response) {
        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new OllamaEmbeddingException("Ollama returned an empty embedding", null);
        }

        List<Double> embedding = response.embeddings().getFirst();
        if (embedding == null || embedding.size() != EMBEDDING_DIMENSIONS) {
            int actualDimensions = embedding == null ? 0 : embedding.size();
            throw new OllamaEmbeddingException(
                    "Ollama embedding dimension mismatch: expected "
                            + EMBEDDING_DIMENSIONS + " but received " + actualDimensions,
                    null);
        }
        if (embedding.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
            throw new OllamaEmbeddingException("Ollama returned an invalid embedding value", null);
        }
        return List.copyOf(embedding);
    }
}
