package com.example.Spotify;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.netty.channel.ChannelOption;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class SpotifyApiClientConfiguration {

    @Bean
    SpotifyAPIClient spotifyAPIClient(
            WebClient.Builder webClientBuilder,
            @Value("${spotify.api.base-url:https://api.spotify.com/v1}") String baseUrl,
            @Value("${spotify.api.connect-timeout:5s}") Duration connectTimeout,
            @Value("${spotify.api.response-timeout:10s}") Duration responseTimeout) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                .responseTimeout(responseTimeout);

        WebClient webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(spotifyErrorHandlingFilter())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(SpotifyAPIClient.class);
    }

    private ExchangeFilterFunction spotifyErrorHandlingFilter() {
        return (request, next) -> next.exchange(request)
                .onErrorMap(this::isTransportFailure,
                        error -> new SpotifyApiException(
                                "Spotify API request timed out or could not be completed", error))
                .flatMap(this::translateErrorResponse);
    }

    private boolean isTransportFailure(Throwable error) {
        Throwable unwrapped = Exceptions.unwrap(error);
        return error instanceof WebClientRequestException
                || unwrapped instanceof TimeoutException;
    }

    private Mono<ClientResponse> translateErrorResponse(ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        if (!status.isError()) {
            return Mono.just(response);
        }

        String retryAfter = response.headers().asHttpHeaders().getFirst("Retry-After");
        return response.bodyToMono(SpotifyErrorResponse.class)
                .map(SpotifyErrorResponse::message)
                .onErrorReturn("")
                .defaultIfEmpty("")
                .flatMap(message -> Mono.error(
                        SpotifyApiException.forResponse(status.value(), message, retryAfter)));
    }

    private record SpotifyErrorResponse(SpotifyError error) {
        String message() {
            return error == null || error.message() == null ? "" : error.message();
        }
    }

    private record SpotifyError(Integer status, String message) {
    }
}
