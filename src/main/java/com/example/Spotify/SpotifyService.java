package com.example.Spotify;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.DTO.CurrentPlaylist;
import com.example.DTO.ResultPlaylist;
import com.example.DTO.Search.SearchResponse;

import reactor.core.publisher.Mono;

@Service
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final SpotifyAPIClient spotifyAPIClient;

    public SpotifyService(
            @Value("${SPOTIFY_CLIENT_ID}") String clientId,
            @Value("${SPOTIFY_CLIENT_SECRET}") String clientSecret,
            @Value("${SPOTIFY_REDIRECT_URI}") String redirectUri,
            SpotifyAPIClient spotifyAPIClient) {

        this.spotifyAPIClient = spotifyAPIClient;

        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(URI.create(redirectUri))
                .build();
    }

    // ============================================================
    // 1. 取得 Spotify OAuth 授權網址
    // ============================================================

    public CompletableFuture<URI> getAuthorizationUri() {

        AuthorizationCodeUriRequest request =
                spotifyApi.authorizationCodeUri()
                        .scope(
                                "user-read-email " +
                                "user-top-read " +
                                "playlist-read-private " +
                                "playlist-modify-private " +
                                "playlist-modify-public"
                        )
                        .show_dialog(true)
                        .build();

        return request.executeAsync();
    }

    // ============================================================
    // 2. Authorization Code 換 Access Token / Refresh Token
    // ============================================================

    public CompletableFuture<AuthorizationCodeCredentials> authorize(String code) {

        AuthorizationCodeRequest request =
                spotifyApi.authorizationCode(code)
                        .build();

        return request.executeAsync()
                .thenApply(credentials -> {

                    String accessToken =
                            credentials.getAccessToken();

                    String refreshToken =
                            credentials.getRefreshToken();

                    spotifyApi.setAccessToken(accessToken);

                    if (refreshToken != null && !refreshToken.isBlank()) {
                        spotifyApi.setRefreshToken(refreshToken);
                    }

                    System.out.println("Spotify authorization successful");
                    System.out.println(
                            "Access token exists: "
                                    + (spotifyApi.getAccessToken() != null)
                    );

                    System.out.println(
                            "Refresh token exists: "
                                    + (spotifyApi.getRefreshToken() != null)
                    );

                    return credentials;
                });
    }

    // ============================================================
    // 3. Refresh Access Token
    // ============================================================

    public CompletableFuture<AuthorizationCodeCredentials> refreshAccessToken() {

        String refreshToken = spotifyApi.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "Spotify refresh token 不存在，請重新進行 Spotify OAuth 授權"
                    )
            );
        }

        AuthorizationCodeRefreshRequest request =
                spotifyApi.authorizationCodeRefresh()
                        .build();

        return request.executeAsync()
                .thenApply(credentials -> {

                    String newAccessToken =
                            credentials.getAccessToken();

                    spotifyApi.setAccessToken(newAccessToken);

                    /*
                     * Spotify refresh 時不一定會回傳新的 refresh token。
                     * 如果有回傳才更新。
                     * 沒有的話繼續使用原本的 refresh token。
                     */
                    if (credentials.getRefreshToken() != null
                            && !credentials.getRefreshToken().isBlank()) {

                        spotifyApi.setRefreshToken(
                                credentials.getRefreshToken()
                        );
                    }

                    System.out.println("Spotify access token refreshed");

                    return credentials;
                });
    }

    // ============================================================
    // 4. 取得一個可使用的最新 Access Token
    // ============================================================

    public CompletableFuture<String> getValidAccessToken() {

        String refreshToken = spotifyApi.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "Spotify 尚未完成授權，沒有 Refresh Token，請先呼叫 authorizeSpotify"
                    )
            );
        }

        return refreshAccessToken()
                .thenApply(credentials ->
                        credentials.getAccessToken()
                );
    }

    // ============================================================
    // 5. 搜尋歌曲
    // ============================================================
    public Mono<SearchResponse> searchSpotify(String q,String type,int limit,int offset) 
    {

        return Mono.fromFuture(getValidAccessToken())
                .flatMap(accessToken ->
                        spotifyAPIClient.searchSpotify(
                                accessToken,
                                q,
                                type,
                                limit,
                                offset
                        )
                );
    }

    // ============================================================
    // 6. 取得使用者 Top Tracks
    // ============================================================

    public CompletableFuture<Paging<Track>> getTopTracks() {
    return getValidAccessToken()
            .thenCompose(accessToken -> {
                spotifyApi.setAccessToken(accessToken);

                return spotifyApi.getUsersTopTracks()
                        .limit(10)
                        .build()
                        .executeAsync();
            });
}

    // ============================================================
    // 7. 取得目前使用者 Playlist
    // ============================================================

    public Mono<CurrentPlaylist> getCurrentPlaylist(
            Integer limit,
            Integer offset) {

        int finalLimit =
                (limit != null) ? limit : 20;

        int finalOffset =
                (offset != null) ? offset : 0;

        return Mono
                .fromFuture(getValidAccessToken())
                .flatMap(accessToken ->

                        spotifyAPIClient.getCurrentPlaylist(
                                accessToken,
                                finalLimit,
                                finalOffset
                        )
                );
    }

    // ============================================================
    // 8. 建立 Playlist
    // ============================================================

    public Mono<ResultPlaylist> createPlaylist(
            String playlistName) {

        return Mono
                .fromFuture(getValidAccessToken())
                .flatMap(accessToken ->

                        spotifyAPIClient.createPlaylist(
                                accessToken,
                                playlistName
                        )
                );
    }

    // ============================================================
    // 9. 加歌曲到 Playlist
    // ============================================================

    public CompletableFuture<SnapshotResult> addTrackToPlaylist(
            String playlistId,
            String trackUri) {

        return getValidAccessToken()
                .thenCompose(accessToken -> {

                    String[] uris = {
                            trackUri
                    };

                    return spotifyApi
                            .addItemsToPlaylist(
                                    playlistId,
                                    uris
                            )
                            .build()
                            .executeAsync();
                });
    }

    // ============================================================
    // 10. Token 狀態檢查
    // ============================================================

    public String getTokenStatus() {

        boolean accessTokenExists =
                spotifyApi.getAccessToken() != null
                        && !spotifyApi.getAccessToken().isBlank();

        boolean refreshTokenExists =
                spotifyApi.getRefreshToken() != null
                        && !spotifyApi.getRefreshToken().isBlank();

        return "Access Token exists: "
                + accessTokenExists
                + "\nRefresh Token exists: "
                + refreshTokenExists;
    }
}
