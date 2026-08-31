package com.example.Spotify;

import com.example.DTO.Search.SearchResponse;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.DTO.CurrentPlaylist;
import com.example.DTO.ResultPlaylist;

import reactor.core.publisher.Mono;

@Component
public class SpotifyAPIClient {

    private final WebClient client;

    // 建立 Spotify API 的 HTTP Client
    public SpotifyAPIClient() {
        this.client = WebClient.builder()
                .baseUrl("https://api.spotify.com/v1")
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    //取得playlists
    public Mono<CurrentPlaylist> getCurrentPlaylist(String accessToken,int limit,int offset)
    {

         return client.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/me/playlists")
                    .queryParam("limit", limit)
                    .queryParam("offset",offset)
                    .build())
            .headers(h -> h.setBearerAuth(accessToken))
            .retrieve()
            .bodyToMono(CurrentPlaylist.class);
    }


    // 建立 Playlist
    public Mono<ResultPlaylist> createPlaylist(
            String accessToken,
            String playlistName) {

        // 要傳給 Spotify 的 JSON Body
        Map<String, Object> requestBody = Map.of(
                "name", playlistName,
                "public", false,
                "description", "Created by Discord Bot"
        );

        // 發送 POST Request
        return client.post()
                .uri("/me/playlists")
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ResultPlaylist.class);
    }

    //搜尋歌曲或相關資源
    public Mono<SearchResponse> searchSpotify(String accessToken,String q,String type,int limit,int offset) 
    {

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", q)
                        .queryParam("type", type)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .headers(headers ->
                        headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(SearchResponse.class);
     }
}
