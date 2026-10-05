package com.example.song.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.example.song.DTO.SongSemanticCandidate;

@Repository
public class SongEmbeddingRepository {

    private static final String NEAREST_SONGS_SQL = """
            SELECT
                id,
                song,
                artist,
                chunk_text,
                link,
                chunk_index,
                embedding <=> CAST(:queryEmbedding AS vector) AS distance
            FROM song_embeddings
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :candidateLimit
            """;

    private final JdbcClient jdbcClient;

    public SongEmbeddingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SongSemanticCandidate> findNearest(List<Double> queryEmbedding, int candidateLimit) {
        String vectorLiteral = queryEmbedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));

        return jdbcClient.sql(NEAREST_SONGS_SQL)
                .param("queryEmbedding", vectorLiteral)
                .param("candidateLimit", candidateLimit)
                .query((resultSet, rowNumber) -> {
                    double distance = resultSet.getDouble("distance");
                    return new SongSemanticCandidate(
                            resultSet.getLong("id"),
                            resultSet.getString("song"),
                            resultSet.getString("artist"),
                            resultSet.getString("chunk_text"),
                            resultSet.getString("link"),
                            resultSet.getInt("chunk_index"),
                            1.0 - distance,
                            distance);
                })
                .list();
    }
}
