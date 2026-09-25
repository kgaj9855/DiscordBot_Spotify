package com.example.DTO.RecentlyPlayed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecentlyPlayedResponse(
        String href,
        Integer limit,
        String next,
        List<RecentlyPlayedItem> items) {

    public RecentlyPlayedResponse {
        items = immutableCopy(items);
    }

    public static RecentlyPlayedResponse empty() {
        return new RecentlyPlayedResponse(null, 0, null, List.of());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecentlyPlayedItem(
            Track track,
            @JsonProperty("played_at") Instant playedAt,
            PlaybackContext context) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Track(
            String id,
            String name,
            String uri,
            String href,
            @JsonProperty("duration_ms") Integer durationMs,
            Boolean explicit,
            List<Artist> artists,
            Album album) {

        public Track {
            artists = immutableCopy(artists);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Artist(String id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Album(String id, String name, List<Image> images) {

        public Album {
            images = immutableCopy(images);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String url, Integer height, Integer width) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlaybackContext(String type, String uri, String href) {
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(values));
    }
}
