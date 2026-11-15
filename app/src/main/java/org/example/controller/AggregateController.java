package org.example.controller;

import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.example.metrics.AggregateMetrics;
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
    private final AggregateMetrics metrics;

    AggregateController(UserClient userClient,
                        LimitsClient limitsClient,
                        AggregateMetrics metrics) {
        this.userClient = userClient;
        this.limitsClient = limitsClient;
        this.metrics = metrics;
    }

    @GetMapping("/aggregate/{id}")
    Aggregate aggregate(@PathVariable String id) throws InterruptedException {
        var sample = metrics.start();
        try (var scope = StructuredTaskScope.open()) {
            var user = scope.fork(() -> userClient.fetch(id));
            var limits = scope.fork(() -> limitsClient.fetch(id));
            scope.join();
            return new Aggregate(user.get(), limits.get());
        } finally {
            metrics.stop(sample);
        }
    }
}
