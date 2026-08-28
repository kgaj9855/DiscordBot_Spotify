package com.example.Spotify;



import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.net.URI;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.DTO.CurrentPlaylist;
import com.example.DTO.ResultPlaylist;

import reactor.core.publisher.Mono;


@Service
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final SpotifyAPIClient spotifyAPIClient;

    public SpotifyService(
            @Value("${SPOTIFY_CLIENT_ID}") String clientId,
            @Value("${SPOTIFY_CLIENT_SECRET}") String clientSecret,
            @Value("${SPOTIFY_REDIRECT_URI}") String redirectUri,
            SpotifyAPIClient spotifyAPIClient)
        {
                this.spotifyAPIClient = spotifyAPIClient;

                this.spotifyApi = new SpotifyApi.Builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUri(URI.create(redirectUri))
                        .build();
        }

        // 1. 取得 Spotify 授權網址
    public CompletableFuture<URI> getAuthorizationUri() {

        AuthorizationCodeUriRequest request =
                spotifyApi.authorizationCodeUri()
                        .scope(
                                "user-read-email " +
                                "user-top-read " +
                                "playlist-read-private " +
                                "playlist-modify-private " +
                                "playlist-modify-public"
                                ).show_dialog(true).build();

        return request.executeAsync();
    }

    // 2. Authorization Code 換 Access Token
    public CompletableFuture<AuthorizationCodeCredentials>
            authorize(String code) {

        AuthorizationCodeRequest request =
                spotifyApi.authorizationCode(code).build();

        return request.executeAsync()
                .thenApply(credentials -> {

                    spotifyApi.setAccessToken(
                            credentials.getAccessToken()
                    );

                    spotifyApi.setRefreshToken(
                            credentials.getRefreshToken()
                    );

                    return credentials;
                });
    }

    // 3. 更新 Access Token
    public CompletableFuture<AuthorizationCodeCredentials>
            refreshAccessToken() {

        AuthorizationCodeRefreshRequest request =
                spotifyApi.authorizationCodeRefresh()
                        .build();

        return request.executeAsync()
                .thenApply(credentials -> {

                    spotifyApi.setAccessToken(
                            credentials.getAccessToken()
                    );

                    return credentials;
                });
    }

    // 4. 搜尋歌曲
    public CompletableFuture<Paging<Track>>
            searchTracks(String keyword) {

        return spotifyApi.searchTracks(keyword)
                .limit(10)
                .build()
                .executeAsync();
    }

    // 5. 取得使用者 Top Tracks
    public CompletableFuture<Paging<Track>>
            getTopTracks() {

        return spotifyApi.getUsersTopTracks()
                .limit(10)
                .build()
                .executeAsync();
    }

    // 6. 取得 Playlist
   public Mono<CurrentPlaylist> getCurrentPlaylist(Integer limit,Integer offset) 
   {

        int finalLimit = (limit != null) ? limit : 20;
        int finalOffset = (offset != null) ? offset : 0;

        String accessToken = spotifyApi.getAccessToken();

        return spotifyAPIClient.getCurrentPlaylist(
                accessToken,
                finalLimit,
                finalOffset
        );
   }

    // 7. 建立 Playlist
    public Mono<ResultPlaylist> createPlaylist(String playlistName) 
    {

        String accessToken = spotifyApi.getAccessToken();

        return spotifyAPIClient.createPlaylist(
                accessToken,
                playlistName
        );
    }

    // 8. 加歌曲到 Playlist
    public CompletableFuture<SnapshotResult>
            addTrackToPlaylist(
                    String playlistId,
                    String trackUri) {

        String[] uris = {trackUri};

        return spotifyApi
                .addItemsToPlaylist(
                        playlistId,
                        uris
                )
                .build()
                .executeAsync();
    }
}
