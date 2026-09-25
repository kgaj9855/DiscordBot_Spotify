package com.example.Spotify;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import com.example.DTO.RecentlyPlayed.RecentlyPlayedResponse;

import reactor.core.publisher.Mono;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

class SpotifyServiceTest {

    @Test
    void preservesExistingScopesWhenAddingRecentlyPlayedScope() {
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("user-read-email"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("user-top-read"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("playlist-read-private"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("playlist-modify-private"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("playlist-modify-public"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("user-read-playback-state"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("user-modify-playback-state"));
        assertTrue(SpotifyService.AUTHORIZATION_SCOPES.contains("user-read-recently-played"));
    }

    @Test
    void getsRecentlyPlayedWithBearerTokenAndDefaultLimit() {
        SpotifyAPIClient client = mock(SpotifyAPIClient.class);
        SpotifyService service = spy(service(client));
        RecentlyPlayedResponse response = new RecentlyPlayedResponse(null, 20, null, List.of());
        doReturn(CompletableFuture.completedFuture("access-token"))
                .when(service).getValidAccessToken();
        when(client.getRecentlyPlayed("Bearer access-token", 20))
                .thenReturn(Mono.just(response));

        assertSame(response, service.getRecentlyPlayed(null).block());
    }

    @Test
    void refreshesAndRetriesOnceAfterUnauthorizedResponse() {
        SpotifyAPIClient client = mock(SpotifyAPIClient.class);
        SpotifyService service = spy(service(client));
        RecentlyPlayedResponse response = RecentlyPlayedResponse.empty();
        AuthorizationCodeCredentials credentials = mock(AuthorizationCodeCredentials.class);

        doReturn(CompletableFuture.completedFuture("expired-token"))
                .when(service).getValidAccessToken();
        doReturn(CompletableFuture.completedFuture(credentials))
                .when(service).refreshAccessToken();
        when(credentials.getAccessToken()).thenReturn("refreshed-token");
        when(client.getRecentlyPlayed("Bearer expired-token", 20))
                .thenReturn(Mono.error(SpotifyApiException.forResponse(401, "expired", null)));
        when(client.getRecentlyPlayed("Bearer refreshed-token", 20))
                .thenReturn(Mono.just(response));

        assertSame(response, service.getRecentlyPlayed(20).block());

        verify(service).refreshAccessToken();
    }

    @Test
    void rejectsOutOfRangeLimitBeforeCallingSpotify() {
        SpotifyService service = service(mock(SpotifyAPIClient.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.getRecentlyPlayed(51).block());
    }

    private SpotifyService service(SpotifyAPIClient client) {
        return new SpotifyService(
                "client-id",
                "client-secret",
                "https://example.com/callback",
                client);
    }
}
