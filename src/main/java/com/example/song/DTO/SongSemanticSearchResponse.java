package com.example.song.DTO;

import java.util.List;

public record SongSemanticSearchResponse(
        String semanticQuery,
        int returnedCount,
        List<SongSemanticCandidate> candidates) {

    public SongSemanticSearchResponse {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
