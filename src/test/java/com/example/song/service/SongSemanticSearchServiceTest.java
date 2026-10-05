package com.example.song.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.ollama.OllamaEmbeddingService;
import com.example.song.DTO.SongSemanticCandidate;
import com.example.song.repository.SongEmbeddingRepository;

import reactor.core.publisher.Mono;

class SongSemanticSearchServiceTest {

    @Test
    void returnsBoundedDistinctSongsInDistanceOrder() {
        OllamaEmbeddingService embeddingService = mock(OllamaEmbeddingService.class);
        SongEmbeddingRepository repository = mock(SongEmbeddingRepository.class);
        SongSemanticSearchService service = new SongSemanticSearchService(
                embeddingService, repository);
        List<Double> embedding = Collections.nCopies(768, 0.1);
        when(embeddingService.embedSemanticQuery("romantic gentle love"))
                .thenReturn(Mono.just(embedding));
        when(repository.findNearest(embedding, 9)).thenReturn(List.of(
                candidate(1, "Song A", "/song-a", 0, 0.10),
                candidate(2, "Song A", "/song-a", 1, 0.11),
                candidate(3, "Song B", "/song-b", 0, 0.12),
                candidate(4, "Song C", "/song-c", 0, 0.13),
                candidate(5, "Song D", "/song-d", 0, 0.14)));

        var response = service.search(" romantic gentle love ", 3).block();

        assertEquals("romantic gentle love", response.semanticQuery());
        assertEquals(3, response.returnedCount());
        assertEquals(List.of("Song A", "Song B", "Song C"),
                response.candidates().stream().map(SongSemanticCandidate::trackName).toList());
        verify(repository).findNearest(embedding, 9);
    }

    @Test
    void validatesInputBeforeCallingDownstreamServices() {
        OllamaEmbeddingService embeddingService = mock(OllamaEmbeddingService.class);
        SongEmbeddingRepository repository = mock(SongEmbeddingRepository.class);
        SongSemanticSearchService service = new SongSemanticSearchService(
                embeddingService, repository);

        assertThrows(IllegalArgumentException.class, () -> service.search(" ", 10));
        assertThrows(IllegalArgumentException.class, () -> service.search("query", 21));
        verifyNoInteractions(embeddingService, repository);
    }

    private SongSemanticCandidate candidate(
            long id, String name, String link, int chunkIndex, double distance) {
        return new SongSemanticCandidate(
                id, name, "Artist", "Description", link, chunkIndex,
                1.0 - distance, distance);
    }
}
