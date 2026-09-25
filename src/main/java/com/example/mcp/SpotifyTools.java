package com.example.mcp;

import com.example.DTO.ResultPlaylist;
import com.example.DTO.Playback.PlaybackStateResponse;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedToolResponse;
import com.example.DTO.Search.SearchResponse;
import com.example.Spotify.SpotifyService;
import com.example.playerhistory.service.PlayerHistoryService;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.Paging;

@Component
public class SpotifyTools {

        private final SpotifyService spotifyService;
        private final PlayerHistoryService playerHistoryService;

        public SpotifyTools(
                        SpotifyService spotifyService,
                        PlayerHistoryService playerHistoryService) {
                this.spotifyService = spotifyService;
                this.playerHistoryService = playerHistoryService;
        }

        // 搜尋 Spotify
        @Tool(description = """
                        Search Spotify for tracks, artists, albums, or playlists.

                        Use Spotify search field filters when the user provides
                        specific search conditions.
                        """)
        public SearchResponse searchSpotify(

                        @ToolParam(description = """
                                        Spotify search query.

                                        Supported filters:
                                        - artist:<name>
                                        - track:<name>
                                        - album:<name>
                                        - year:<year>
                                        - year:<start>-<end>
                                        - genre:<genre>
                                        - isrc:<code>

                                        Examples:
                                        - artist:周杰倫 year:2001-2005
                                        - track:Doxy artist:Miles Davis
                                        - artist:Taylor Swift year:2020

                                        Do not URL-encode the query.
                                        """) String q,

                        @ToolParam(description = """
                                        Type of Spotify item to search.

                                        Allowed values:
                                        - track
                                        - artist
                                        - album
                                        - playlist
                                        """) String type,

                        @ToolParam(description = """
                                        Maximum number of results to return.
                                        Use 10 unless the user requests otherwise.
                                        """) int limit,

                        @ToolParam(description = """
                                        Index of the first result to return.
                                        Use 0 unless pagination is requested.
                                        """) int offset) {

                return spotifyService
                                .searchSpotify(q, type, limit, offset)
                                .block();
        }

        // 取得使用者 Top Tracks
        @Tool(description = "Get the user's top Spotify tracks")
        public String getTopTracks() {

                Paging<Track> result = spotifyService
                                .getTopTracks()
                                .join();

                Track[] tracks = result.getItems();

                if (tracks == null || tracks.length == 0) {
                        return "No top tracks found.";
                }

                StringBuilder response = new StringBuilder();

                for (int i = 0; i < tracks.length; i++) {

                        Track track = tracks[i];

                        response.append(i + 1)
                                        .append(". ")
                                        .append(track.getName());

                        if (track.getArtists().length > 0) {
                                response.append(" - ")
                                                .append(track.getArtists()[0].getName());
                        }

                        response.append("\n");
                }

                return response.toString();
        }

        // 取得 Playlist
        @Tool(description = "Get current user's Spotify playlists")
        public Mono<String> getPlaylist(Integer limit, Integer offset) {

                return spotifyService
                                .getCurrentPlaylist(limit, offset)
                                .map(result -> {

                                        return result.getItems()
                                                        .stream()
                                                        .map(playlist -> "Playlist: " + playlist.getName()
                                                                        + "\nID: " + playlist.getId())
                                                        .collect(Collectors.joining("\n\n"));
                                });
        }

        // 建立 Playlist
        @Tool(description = "Create a new private Spotify playlist")
        public String createPlaylist(
                        String playlistName) {

                ResultPlaylist playlist = spotifyService
                                .createPlaylist(playlistName).block();

                if (playlist == null) {
                        return "Failed to create playlist.";
                }

                return "Playlist created successfully."
                                + "\nName: " + playlist.getName()
                                + "\nPlaylist ID: " + playlist.getId();
        }

        // 加歌曲到 Playlist
        @Tool(description = "Add a Spotify track to a playlist")
        public String addTrackToPlaylist(
                        String playlistId,
                        String trackUri) {

                spotifyService
                                .addTrackToPlaylist(
                                                playlistId,
                                                trackUri)
                                .join();

                return "Track added to playlist successfully.";
        }

        // 取得目前播放狀態
        @Tool(description = """
                        Get the user's current Spotify playback state.

                        Returns information about the active device, playback progress,
                        playback status, shuffle state, repeat state, and playback context.
                        """)
        public PlaybackStateResponse getPlaybackState() {

                return spotifyService
                                .getCurrentPlayback()
                                .block();
        }

        @Tool(description = "取得 Spotify OAuth 授權網址，讓使用者連線 Spotify 帳號")
        public String authorizeSpotify() {
                try {
                        URI uri = spotifyService.getAuthorizationUri().join();
                        return "請開啟以下連結完成 Spotify 授權：\n" + uri;
                } catch (Exception e) {
                        return "產生 Spotify 授權網址失敗：" + e.getMessage();
                }
        }

        @Tool(description = """
                        Toggle the user's current Spotify playback state.
                        If a track is playing, pause playback.
                        Returns the Spotify API HTTP status code.
                        """)
        public int pauseSpotifyPlayback() {
                return spotifyService.pausePlayer().block();
        }

        @Tool(description = """
                        Toggle the user's current Spotify playback state.
                        If playback is paused, resume and start playback.
                        Returns the Spotify API HTTP status code.
                        """)
        public int resumeSpotifyPlayback() {
                return spotifyService.resumePlayer().block();
        }

        @Tool(description = """
                        Get the current user's recently played Spotify tracks and synchronize them to PostgreSQL.

                        Use this tool when the user asks about songs they recently listened to,
                        recent playback, Spotify recently played history, or phrases such as:
                        「最近聽過的歌曲」、「我最近聽了什麼」、「Spotify 最近播放」、
                        「最近播放紀錄」或「幫我看看最近聽什麼歌」。

                        The response contains a bounded list of tracks plus fetchedCount and savedCount.
                        Repeated synchronization does not insert the same track_id and played_at twice.
                        """)
        public RecentlyPlayedToolResponse getRecentlyPlayed(
                        @ToolParam(description = """
                                        Maximum number of recent plays to fetch from Spotify.
                                        Optional; defaults to 20. Allowed range is 1 to 50.
                                        """, required = false) Integer limit) {

                RecentlyPlayedResponse response = spotifyService
                                .getRecentlyPlayed(limit)
                                .block();

                RecentlyPlayedResponse safeResponse = response == null
                                ? RecentlyPlayedResponse.empty()
                                : response;
                int savedCount = playerHistoryService.syncRecentlyPlayed(safeResponse.items());

                return RecentlyPlayedToolResponse.from(safeResponse.items(), savedCount);
        }
}
