package org.example.controller;

import org.example.rates.RateService;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

class RatesControllerTest {

    private final RatesController controller = new RatesController(new RateService());

    @Test
    void streamsOneRatePerSecond() {
        StepVerifier.withVirtualTime(controller::rates)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNextMatches(rate -> rate.pair().equals("USD/RUB"))
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNextCount(1)
                .thenAwait(Duration.ofSeconds(3))
                .expectNextCount(3)
                .thenCancel()
                .verify(Duration.ofSeconds(1));
    }
}
