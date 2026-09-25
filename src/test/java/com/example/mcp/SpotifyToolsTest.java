package com.example.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Album;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Artist;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.RecentlyPlayedItem;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.Track;
import com.example.Spotify.SpotifyService;
import com.example.playerhistory.service.PlayerHistoryService;

import reactor.core.publisher.Mono;

class SpotifyToolsTest {

    @Test
    void fetchesPersistsAndReturnsMcpFriendlyResponse() {
        SpotifyService spotifyService = mock(SpotifyService.class);
        PlayerHistoryService historyService = mock(PlayerHistoryService.class);
        SpotifyTools tools = new SpotifyTools(spotifyService, historyService);
        RecentlyPlayedItem item = new RecentlyPlayedItem(
                new Track(
                        "track-1", "Song A", "spotify:track:track-1", null,
                        1000, false,
                        List.of(new Artist("artist-1", "Artist A")),
                        new Album("album-1", "Album A", List.of())),
                Instant.parse("2026-09-25T02:00:00Z"),
                null);
        RecentlyPlayedResponse spotifyResponse = new RecentlyPlayedResponse(
                null, 20, null, List.of(item));
        when(spotifyService.getRecentlyPlayed(20)).thenReturn(Mono.just(spotifyResponse));
        when(historyService.syncRecentlyPlayed(spotifyResponse.items())).thenReturn(1);

        var response = tools.getRecentlyPlayed(20);

        assertEquals(1, response.fetchedCount());
        assertEquals(1, response.savedCount());
        assertEquals("Song A", response.tracks().getFirst().trackName());
        assertEquals("Artist A", response.tracks().getFirst().artistName());
        assertEquals("spotify:track:track-1", response.tracks().getFirst().spotifyUri());
        verify(historyService).syncRecentlyPlayed(spotifyResponse.items());
    }

    @Test
    void returnsEmptyResultWhenSpotifyHasNoHistory() {
        SpotifyService spotifyService = mock(SpotifyService.class);
        PlayerHistoryService historyService = mock(PlayerHistoryService.class);
        SpotifyTools tools = new SpotifyTools(spotifyService, historyService);
        when(spotifyService.getRecentlyPlayed(null))
                .thenReturn(Mono.just(RecentlyPlayedResponse.empty()));

        var response = tools.getRecentlyPlayed(null);

        assertEquals(0, response.fetchedCount());
        assertEquals(0, response.savedCount());
        assertEquals(List.of(), response.tracks());
    }

    @Test
    void excludesIncompleteSpotifyItemsFromMcpTracks() {
        SpotifyService spotifyService = mock(SpotifyService.class);
        PlayerHistoryService historyService = mock(PlayerHistoryService.class);
        SpotifyTools tools = new SpotifyTools(spotifyService, historyService);
        RecentlyPlayedItem incomplete = new RecentlyPlayedItem(
                new Track(null, null, null, null, null, null, null, null),
                Instant.parse("2026-09-25T02:00:00Z"),
                null);
        RecentlyPlayedResponse spotifyResponse = new RecentlyPlayedResponse(
                null, 20, null, List.of(incomplete));
        when(spotifyService.getRecentlyPlayed(20)).thenReturn(Mono.just(spotifyResponse));

        var response = tools.getRecentlyPlayed(20);

        assertEquals(1, response.fetchedCount());
        assertEquals(List.of(), response.tracks());
    }
}
