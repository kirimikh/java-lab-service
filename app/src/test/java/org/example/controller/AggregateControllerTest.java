package org.example.controller;

import org.example.client.Limits;
import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregateControllerTest {

    private final LimitsClient limitsClient = mock(LimitsClient.class);
    private final AggregateController controller =
            new AggregateController(new UserClient(), limitsClient);

    @Test
    void aggregatesBothSources() {
        when(limitsClient.fetch("42")).thenReturn(Mono.just(new Limits("42",
                new BigDecimal("50000.00"))));

        StepVerifier.create(controller.aggregate("42"))
                .expectNextMatches(a -> a.user().id().equals("42")
                        && a.limits().daily().equals(new BigDecimal("50000.00")))
                .verifyComplete();
    }

    @Test
    void fallsBackOnTimeout() {
        when(limitsClient.fetch("42")).thenReturn(Mono.never()); // источник так и не ответит

        StepVerifier.create(controller.aggregate("42"))
                .expectNext(Aggregate.fallback("42"))
                .verifyComplete();
    }

    @Test
    void propagatesSourceError() {
        when(limitsClient.fetch("fail")).thenReturn(Mono.never());

        StepVerifier.create(controller.aggregate("fail"))
                .verifyError(IllegalStateException.class);
    }
}
