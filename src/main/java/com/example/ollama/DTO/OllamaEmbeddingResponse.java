package com.example.ollama.DTO;

import java.util.List;

public record OllamaEmbeddingResponse(
        String model,
        List<List<Double>> embeddings) {
}
