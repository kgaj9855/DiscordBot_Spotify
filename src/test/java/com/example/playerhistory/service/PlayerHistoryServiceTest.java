package com.example.playerhistory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Album;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Artist;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.PlaybackContext;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.RecentlyPlayedItem;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Track;
import com.example.playerhistory.entity.PlayerHistory;
import com.example.playerhistory.repository.PlayerHistoryRepository;

class PlayerHistoryServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-25T03:00:00Z");

    private PlayerHistoryRepository repository;
    private PlayerHistoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerHistoryRepository.class);
        service = new PlayerHistoryService(
                repository,
                Clock.fixed(CREATED_AT, ZoneOffset.UTC));
    }

    @Test
    void savesSameTrackAtDifferentPlaybackTimes() {
        whenInsert().thenReturn(1);

        int saved = service.syncRecentlyPlayed(List.of(
                item("track-1", "2026-09-25T02:00:00Z"),
                item("track-1", "2026-09-25T03:00:00Z")));

        assertEquals(2, saved);
        ArgumentCaptor<PlayerHistory> captor = ArgumentCaptor.forClass(PlayerHistory.class);
        verify(repository, times(2)).insertIfAbsent(captor.capture());
        assertEquals(Instant.parse("2026-09-25T02:00:00Z"), captor.getAllValues().get(0).getPlayedAt());
        assertEquals(Instant.parse("2026-09-25T03:00:00Z"), captor.getAllValues().get(1).getPlayedAt());
        assertEquals("track-1", captor.getAllValues().get(0).getTrackId());
        assertEquals("Artist", captor.getAllValues().get(0).getArtistName());
        assertEquals(CREATED_AT, captor.getAllValues().get(0).getCreatedAt());
    }

    @Test
    void countsOnlyRowsInsertedByDatabaseConflictHandling() {
        whenInsert().thenReturn(1, 0);
        RecentlyPlayedItem duplicate = item("track-1", "2026-09-25T02:00:00Z");

        int saved = service.syncRecentlyPlayed(List.of(duplicate, duplicate));

        assertEquals(1, saved);
        verify(repository, times(2)).insertIfAbsent(any(PlayerHistory.class));
    }

    @Test
    void skipsMalformedItemsAndAllowsNullableSpotifyFields() {
        whenInsert().thenReturn(1);
        List<RecentlyPlayedItem> items = new ArrayList<>();
        items.add(null);
        items.add(new RecentlyPlayedItem(
                new Track(null, "Missing id", null, null, null, null, List.of(), null),
                Instant.parse("2026-09-25T02:00:00Z"),
                null));
        items.add(new RecentlyPlayedItem(
                new Track("track-2", "Song", null, null, null, null, List.of(), null),
                Instant.parse("2026-09-25T04:00:00Z"),
                null));

        int saved = service.syncRecentlyPlayed(items);

        assertEquals(1, saved);
        ArgumentCaptor<PlayerHistory> captor = ArgumentCaptor.forClass(PlayerHistory.class);
        verify(repository).insertIfAbsent(captor.capture());
        assertEquals("track-2", captor.getValue().getTrackId());
        assertEquals(Instant.parse("2026-09-25T04:00:00Z"), captor.getValue().getPlayedAt());
        assertNull(captor.getValue().getArtistId());
        assertNull(captor.getValue().getAlbumId());
        assertNull(captor.getValue().getContextType());
    }

    @Test
    void translatesDatabaseConnectionFailure() {
        whenInsert().thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThrows(PlayerHistoryPersistenceException.class,
                () -> service.syncRecentlyPlayed(List.of(item("track-1", "2026-09-25T02:00:00Z"))));
    }

    private org.mockito.stubbing.OngoingStubbing<Integer> whenInsert() {
        return when(repository.insertIfAbsent(any(PlayerHistory.class)));
    }

    private RecentlyPlayedItem item(String trackId, String playedAt) {
        return new RecentlyPlayedItem(
                new Track(
                        trackId,
                        "Song",
                        "spotify:track:" + trackId,
                        "https://api.spotify.com/v1/tracks/" + trackId,
                        120000,
                        false,
                        List.of(new Artist("artist-1", "Artist")),
                        new Album("album-1", "Album", List.of())),
                Instant.parse(playedAt),
                new PlaybackContext("playlist", "spotify:playlist:1", null));
    }
}
