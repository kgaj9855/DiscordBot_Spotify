package com.example.playerhistory.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Album;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Artist;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.PlaybackContext;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.RecentlyPlayedItem;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Track;
import com.example.playerhistory.entity.PlayerHistory;
import com.example.playerhistory.repository.PlayerHistoryRepository;

@Service
public class PlayerHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PlayerHistoryService.class);

    private final PlayerHistoryRepository repository;
    private final Clock clock;

    public PlayerHistoryService(PlayerHistoryRepository repository) {
        this(repository, Clock.systemUTC());
    }

    PlayerHistoryService(PlayerHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public int syncRecentlyPlayed(List<RecentlyPlayedItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        int savedCount = 0;
        try {
            for (RecentlyPlayedItem item : items) {
                Optional<PlayerHistory> history = toPlayerHistory(item);
                if (history.isEmpty()) {
                    log.warn("Skipping malformed Spotify recently played item");
                    continue;
                }
                PlayerHistory value = history.get();
                savedCount += repository.insertIfAbsent(value);
            }
            return savedCount;
        } catch (DataAccessException error) {
            log.error("Failed to synchronize Spotify recently played history with PostgreSQL", error);
            throw new PlayerHistoryPersistenceException(
                    "Unable to save Spotify recently played history", error);
        }
    }

    private Optional<PlayerHistory> toPlayerHistory(RecentlyPlayedItem item) {
        if (item == null || item.track() == null || item.playedAt() == null) {
            return Optional.empty();
        }

        Track track = item.track();
        if (isBlank(track.id()) || isBlank(track.name())) {
            return Optional.empty();
        }

        Artist artist = track.artists().isEmpty() ? null : track.artists().getFirst();
        Album album = track.album();
        PlaybackContext context = item.context();
        Instant createdAt = clock.instant();

        return Optional.of(new PlayerHistory(
                track.id(),
                track.name(),
                track.uri(),
                artist == null ? null : artist.id(),
                artist == null ? null : artist.name(),
                album == null ? null : album.id(),
                album == null ? null : album.name(),
                item.playedAt(),
                context == null ? null : context.type(),
                context == null ? null : context.uri(),
                createdAt));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
