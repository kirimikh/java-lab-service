package org.example.controller;

import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.StructuredTaskScope;

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
    Aggregate aggregate(@PathVariable String id) throws InterruptedException {
        try (var scope = StructuredTaskScope.open()) {
            var user = scope.fork(() -> userClient.fetch(id));
            var limits = scope.fork(() -> limitsClient.fetch(id));
            scope.join();
            return new Aggregate(user.get(), limits.get());
        }
    }
}
