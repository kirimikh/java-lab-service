package org.example.controller;

import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api")
class AggregateController {

    private final UserClient userClient;
    private final LimitsClient limitsClient;

    AggregateController(UserClient userClient, LimitsClient limitsClient) {
        this.userClient = userClient;
        this.limitsClient = limitsClient;
    }

    @GetMapping("/aggregate/{id}")
    Mono<Aggregate> aggregate(@PathVariable String id) {
        return Mono.zip(userClient.fetch(id), limitsClient.fetch(id))
                .map(t -> new Aggregate(t.getT1(), t.getT2()))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(TimeoutException.class, e -> Mono.just(Aggregate.fallback(id)));
    }
}
