package com.example.song.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.ollama.OllamaEmbeddingService;
import com.example.song.DTO.SongSemanticCandidate;
import com.example.song.DTO.SongSemanticSearchResponse;
import com.example.song.repository.SongEmbeddingRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SongSemanticSearchService {

    static final int DEFAULT_TOP_K = 10;
    static final int MAX_TOP_K = 20;
    static final int MAX_QUERY_LENGTH = 1000;
    private static final int MAX_CHUNKS_PER_SONG = 3;

    private final OllamaEmbeddingService embeddingService;
    private final SongEmbeddingRepository repository;

    public SongSemanticSearchService(
            OllamaEmbeddingService embeddingService,
            SongEmbeddingRepository repository) {
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public Mono<SongSemanticSearchResponse> search(String semanticQuery, Integer topK) {
        String normalizedQuery = validateQuery(semanticQuery);
        int resultLimit = validateTopK(topK);
        int candidateLimit = resultLimit * MAX_CHUNKS_PER_SONG;

        return embeddingService.embedSemanticQuery(normalizedQuery)
                .flatMap(embedding -> Mono.fromCallable(
                                () -> repository.findNearest(embedding, candidateLimit))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(candidates -> toResponse(normalizedQuery, candidates, resultLimit))
                .onErrorMap(DataAccessException.class,
                        error -> new SongSemanticSearchException(
                                "Unable to search song embeddings in PostgreSQL", error));
    }

    private SongSemanticSearchResponse toResponse(
            String semanticQuery,
            List<SongSemanticCandidate> candidates,
            int resultLimit) {
        LinkedHashMap<String, SongSemanticCandidate> uniqueSongs = new LinkedHashMap<>();
        for (SongSemanticCandidate candidate : candidates) {
            uniqueSongs.putIfAbsent(candidate.sourceUrl(), candidate);
        }

        List<SongSemanticCandidate> results = uniqueSongs.values().stream()
                .limit(resultLimit)
                .toList();
        return new SongSemanticSearchResponse(semanticQuery, results.size(), results);
    }

    private String validateQuery(String semanticQuery) {
        if (semanticQuery == null || semanticQuery.isBlank()) {
            throw new IllegalArgumentException("Semantic query must not be blank");
        }
        String normalized = semanticQuery.trim();
        if (normalized.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Semantic query must not exceed " + MAX_QUERY_LENGTH + " characters");
        }
        return normalized;
    }

    private int validateTopK(Integer topK) {
        int value = topK == null ? DEFAULT_TOP_K : topK;
        if (value < 1 || value > MAX_TOP_K) {
            throw new IllegalArgumentException("topK must be between 1 and " + MAX_TOP_K);
        }
        return value;
    }
}
