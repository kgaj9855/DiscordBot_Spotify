package com.example.playerhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.playerhistory.entity.PlayerHistory;

public interface PlayerHistoryRepository extends JpaRepository<PlayerHistory, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO player_history (
                track_id, track_name, track_uri,
                artist_id, artist_name,
                album_id, album_name,
                played_at, context_type, context_uri, created_at
            ) VALUES (
                :#{#history.trackId}, :#{#history.trackName}, :#{#history.trackUri},
                :#{#history.artistId}, :#{#history.artistName},
                :#{#history.albumId}, :#{#history.albumName},
                :#{#history.playedAt}, :#{#history.contextType}, :#{#history.contextUri},
                :#{#history.createdAt}
            )
            ON CONFLICT (track_id, played_at) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("history") PlayerHistory history);
}
