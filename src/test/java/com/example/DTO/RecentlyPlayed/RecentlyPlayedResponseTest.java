package com.example.DTO.RecentlyPlayed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RecentlyPlayedResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deserializesSpotifyRecentlyPlayedPayload() throws Exception {
        String json = """
                {
                  "href": "https://api.spotify.com/v1/me/player/recently-played",
                  "limit": 20,
                  "items": [{
                    "played_at": "2026-09-25T02:00:00Z",
                    "context": {
                      "type": "playlist",
                      "uri": "spotify:playlist:context-1",
                      "href": "https://api.spotify.com/v1/playlists/context-1"
                    },
                    "track": {
                      "id": "track-1",
                      "name": "Song A",
                      "uri": "spotify:track:track-1",
                      "href": "https://api.spotify.com/v1/tracks/track-1",
                      "duration_ms": 123000,
                      "explicit": false,
                      "artists": [{"id": "artist-1", "name": "Artist A"}],
                      "album": {
                        "id": "album-1",
                        "name": "Album A",
                        "images": [{"url": "https://image/cover.jpg", "height": 640, "width": 640}]
                      }
                    }
                  }]
                }
                """;

        RecentlyPlayedResponse response = objectMapper.readValue(json, RecentlyPlayedResponse.class);

        assertEquals(1, response.items().size());
        var item = response.items().getFirst();
        assertEquals(Instant.parse("2026-09-25T02:00:00Z"), item.playedAt());
        assertEquals("track-1", item.track().id());
        assertEquals("Artist A", item.track().artists().getFirst().name());
        assertEquals("Album A", item.track().album().name());
        assertEquals("https://image/cover.jpg", item.track().album().images().getFirst().url());
        assertEquals("playlist", item.context().type());
    }

    @Test
    void treatsMissingItemsAndNestedListsAsEmpty() throws Exception {
        RecentlyPlayedResponse empty = objectMapper.readValue("{}", RecentlyPlayedResponse.class);
        RecentlyPlayedResponse withTrack = objectMapper.readValue(
                "{\"items\":[{\"track\":{\"id\":\"track-1\"},\"played_at\":\"2026-09-25T02:00:00Z\"}]}",
                RecentlyPlayedResponse.class);

        assertTrue(empty.items().isEmpty());
        assertTrue(withTrack.items().getFirst().track().artists().isEmpty());
    }
}
