package com.example.song.DTO;

public record SongSemanticCandidate(
        long songId,
        String trackName,
        String artist,
        String semanticDescription,
        String sourceUrl,
        int chunkIndex,
        double similarity,
        double distance) {
}
