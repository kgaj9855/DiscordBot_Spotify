package com.example.mcp;

import com.example.DTO.ResultPlaylist;
import com.example.Spotify.SpotifyService;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.Paging;

@Component
public class SpotifyTools {

    private final SpotifyService spotifyService;

    public SpotifyTools(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    // 搜尋歌曲
    @Tool(description = "Search Spotify tracks by keyword")
    public String searchTrack(String keyword) {

        Paging<Track> result =
                spotifyService
                        .searchTracks(keyword)
                        .join();

        Track[] tracks = result.getItems();

        if (tracks == null || tracks.length == 0) {
            return "No tracks found.";
        }

        Track track = tracks[0];

        String artistName = track.getArtists().length > 0
                ? track.getArtists()[0].getName()
                : "Unknown Artist";

        return "Song: " + track.getName()
                + "\nArtist: " + artistName
                + "\nSpotify URI: " + track.getUri();
    }


    // 取得使用者 Top Tracks
    @Tool(description = "Get the user's top Spotify tracks")
    public String getTopTracks() {

        Paging<Track> result =
                spotifyService
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
    public Mono<String> getPlaylist(Integer limit, Integer offset) 
    {

        return spotifyService
                .getCurrentPlaylist(limit, offset)
                .map(result -> {

                        return result.getItems()
                                .stream()
                                .map(playlist ->
                                        "Playlist: " + playlist.getName()
                                        + "\nID: " + playlist.getId()
                                )
                                .collect(Collectors.joining("\n\n"));
                });
    }


    // 建立 Playlist
    @Tool(description = "Create a new private Spotify playlist")
    public String createPlaylist(
            String playlistName) {

        ResultPlaylist playlist =
                spotifyService
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
                        trackUri
                )
                .join();

        return "Track added to playlist successfully.";
    }

    @Tool(description = "取得 Spotify OAuth 授權網址，讓使用者連線 Spotify 帳號")
     public String authorizeSpotify() 
     {
        try {
                URI uri = spotifyService.getAuthorizationUri().join();
                return "請開啟以下連結完成 Spotify 授權：\n" + uri;
        } catch (Exception e) {
                return "產生 Spotify 授權網址失敗：" + e.getMessage();
        }
     }
}
