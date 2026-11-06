package org.example.controller;

import org.example.client.Limits;
import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregateControllerTest {

    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(1);
    private static final Limits LIMITS = new Limits("42", new BigDecimal("50000.00"));

    private final LimitsClient limitsClient = mock(LimitsClient.class);
    private final AggregateController controller =
            new AggregateController(new UserClient(), limitsClient);

    @Test
    void waitsForSlowestSourceOnly() {
        when(limitsClient.fetch("42")).thenAnswer(inv -> Mono.delay(Duration.ofMillis(1500))
                .thenReturn(LIMITS));

        StepVerifier.withVirtualTime(() -> controller.aggregate("42"))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(1500))
                .expectNextMatches(a -> a.user().id().equals("42") && a.limits().equals(LIMITS))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void fallsBackOnTimeout() {
        when(limitsClient.fetch("42")).thenReturn(Mono.never());

        StepVerifier.withVirtualTime(() -> controller.aggregate("42"))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(2))
                .expectNext(Aggregate.fallback("42"))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void propagatesSourceError() {
        when(limitsClient.fetch("fail")).thenReturn(Mono.never());

        StepVerifier.withVirtualTime(() -> controller.aggregate("fail"))
                .expectSubscription()
                .thenAwait(Duration.ofMillis(200))
                .expectError(IllegalStateException.class)
                .verify(VERIFY_TIMEOUT);
    }
}
