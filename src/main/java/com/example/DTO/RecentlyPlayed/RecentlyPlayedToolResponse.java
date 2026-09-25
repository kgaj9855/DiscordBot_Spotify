package com.example.DTO.RecentlyPlayed;

import java.time.Instant;
import java.util.List;

import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse.RecentlyPlayedItem;

public record RecentlyPlayedToolResponse(
        int fetchedCount,
        int savedCount,
        List<Track> tracks) {

    public RecentlyPlayedToolResponse {
        tracks = tracks == null ? List.of() : List.copyOf(tracks);
    }

    public static RecentlyPlayedToolResponse from(List<RecentlyPlayedItem> items, int savedCount) {
        List<RecentlyPlayedItem> safeItems = items == null ? List.of() : items;
        List<Track> tracks = safeItems.stream()
                .filter(RecentlyPlayedToolResponse::isComplete)
                .map(item -> new Track(
                        item.track().name(),
                        firstArtistName(item),
                        item.track().album() == null ? null : item.track().album().name(),
                        item.playedAt(),
                        item.track().uri()))
                .toList();
        return new RecentlyPlayedToolResponse(safeItems.size(), savedCount, tracks);
    }

    private static String firstArtistName(RecentlyPlayedItem item) {
        if (item.track().artists().isEmpty() || item.track().artists().getFirst() == null) {
            return null;
        }
        return item.track().artists().getFirst().name();
    }

    private static boolean isComplete(RecentlyPlayedItem item) {
        return item != null
                && item.track() != null
                && item.track().id() != null
                && !item.track().id().isBlank()
                && item.track().name() != null
                && !item.track().name().isBlank()
                && item.playedAt() != null;
    }

    public record Track(
            String trackName,
            String artistName,
            String albumName,
            Instant playedAt,
            String spotifyUri) {
    }
}
