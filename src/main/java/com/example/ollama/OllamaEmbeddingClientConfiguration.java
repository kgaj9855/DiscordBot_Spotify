package com.example.ollama;

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
public class OllamaEmbeddingClientConfiguration {

    @Bean
    OllamaEmbeddingClient ollamaEmbeddingClient(
            WebClient.Builder webClientBuilder,
            @Value("${OLLAMA_BASE_URL}") String baseUrl,
            @Value("${OLLAMA_CONNECT_TIMEOUT:5s}") Duration connectTimeout,
            @Value("${OLLAMA_RESPONSE_TIMEOUT:60s}") Duration responseTimeout) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                .responseTimeout(responseTimeout);

        WebClient webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(ollamaErrorHandlingFilter())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(OllamaEmbeddingClient.class);
    }

    private ExchangeFilterFunction ollamaErrorHandlingFilter() {
        return (request, next) -> next.exchange(request)
                .onErrorMap(this::isTransportFailure,
                        error -> new OllamaEmbeddingException(
                                "Ollama embedding service timed out or is unavailable", error))
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

        return response.bodyToMono(OllamaErrorResponse.class)
                .map(OllamaErrorResponse::error)
                .onErrorReturn("")
                .defaultIfEmpty("")
                .flatMap(message -> Mono.error(
                        OllamaEmbeddingException.forResponse(status.value(), message)));
    }

    private record OllamaErrorResponse(String error) {
    }
}
