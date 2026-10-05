CREATE INDEX IF NOT EXISTS idx_song_embeddings_embedding_hnsw
    ON song_embeddings
    USING hnsw (embedding vector_cosine_ops);
