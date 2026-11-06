package org.example.client;

import org.example.config.LimitsClientProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class LimitsClient {

    private final WebClient webClient;

    public LimitsClient(WebClient.Builder builder, LimitsClientProperties properties) {
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public Mono<Limits> fetch(String id) {
        return webClient.get()
                .uri("/api/limits/{id}", id)
                .retrieve()
                .bodyToMono(Limits.class);
    }
}
