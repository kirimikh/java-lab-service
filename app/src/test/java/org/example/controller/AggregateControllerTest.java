package org.example.controller;

import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

class AggregateControllerTest {

    private final AggregateController controller =
            new AggregateController(new UserClient(), new LimitsClient());

    @Test
    void aggregatesBothSources() {
        StepVerifier.create(controller.aggregate("42"))
                .expectNextMatches(a -> a.user().id().equals("42")
                        && a.limits().daily().equals(new BigDecimal("50000.00")))
                .verifyComplete();
    }

    @Test
    void fallsBackOnTimeout() {
        StepVerifier.create(controller.aggregate("slow"))
                .expectNext(Aggregate.fallback("slow"))
                .verifyComplete();
    }

    @Test
    void propagatesSourceError() {
        StepVerifier.create(controller.aggregate("fail"))
                .verifyError(IllegalStateException.class);
    }
}
