package com.example.Spotify;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.example.DTO.CurrentPlaylist;
import com.example.DTO.ResultPlaylist;
import com.example.DTO.Playback.PlaybackStateResponse;
import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse;
import com.example.DTO.Search.SearchResponse;

import reactor.core.publisher.Mono;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface SpotifyAPIClient {

    @GetExchange("/me/playlists")
    Mono<CurrentPlaylist> getCurrentPlaylist(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset);

    @PostExchange(value = "/me/playlists", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResultPlaylist> createPlaylist(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Map<String, Object> requestBody);

    @GetExchange("/search")
    Mono<SearchResponse> searchSpotify(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam("q") String query,
            @RequestParam("type") String type,
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset);

    @GetExchange("/me/player")
    Mono<PlaybackStateResponse> getCurrentPlayback(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @PutExchange("/me/player/pause")
    Mono<ResponseEntity<Void>> pausePlayer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @PutExchange("/me/player/play")
    Mono<ResponseEntity<Void>> resumePlayer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @GetExchange("/me/player/recently-played")
    Mono<RecentlyPlayedResponse> getRecentlyPlayed(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam("limit") int limit);
}
