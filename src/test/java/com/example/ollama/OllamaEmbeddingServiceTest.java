package com.example.ollama;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.ollama.DTO.OllamaEmbeddingRequest;
import com.example.ollama.DTO.OllamaEmbeddingResponse;

import reactor.core.publisher.Mono;

class OllamaEmbeddingServiceTest {

    @Test
    void usesConfiguredModelAndNomicSearchQueryPrefix() {
        OllamaEmbeddingClient client = mock(OllamaEmbeddingClient.class);
        List<Double> embedding = Collections.nCopies(768, 0.25);
        when(client.embed(any())).thenReturn(Mono.just(
                new OllamaEmbeddingResponse("nomic-embed-text:latest", List.of(embedding))));
        OllamaEmbeddingService service = new OllamaEmbeddingService(
                client, "nomic-embed-text:latest");

        List<Double> result = service.embedSemanticQuery("  romantic gentle love  ").block();

        assertEquals(768, result.size());
        ArgumentCaptor<OllamaEmbeddingRequest> captor = ArgumentCaptor.forClass(
                OllamaEmbeddingRequest.class);
        verify(client).embed(captor.capture());
        assertEquals("nomic-embed-text:latest", captor.getValue().model());
        assertEquals("search_query: romantic gentle love", captor.getValue().input());
    }

    @Test
    void rejectsUnexpectedEmbeddingDimensions() {
        OllamaEmbeddingClient client = mock(OllamaEmbeddingClient.class);
        when(client.embed(any())).thenReturn(Mono.just(
                new OllamaEmbeddingResponse("nomic-embed-text:latest", List.of(List.of(0.1, 0.2)))));
        OllamaEmbeddingService service = new OllamaEmbeddingService(
                client, "nomic-embed-text:latest");

        assertThrows(
                OllamaEmbeddingException.class,
                () -> service.embedSemanticQuery("quiet study music").block());
    }
}
