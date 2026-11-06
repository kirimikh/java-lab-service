package org.example.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class ReactorWarmupTest {

    @Test
    void mapsAndFilters() {
        Flux<Integer> doubled = Flux.just(1, 2, 3)
                .map(i -> i * 2)
                .filter(i -> i > 2);

        StepVerifier.create(doubled)
                .expectNext(4, 6)
                .verifyComplete();

    }

    @Test
    void endsWithErrorSignal() {
        Flux<Integer> divided = Flux.just(1, 0)
                .map(i -> 10 / i);

        StepVerifier.create(divided)
                .expectNext(10)
                .verifyError(ArithmeticException.class);
    }

}
