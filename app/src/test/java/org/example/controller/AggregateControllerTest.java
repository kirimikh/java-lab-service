package org.example.controller;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.example.metrics.AggregateMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateControllerTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    private final AggregateController controller =
            new AggregateController(
                    new UserClient(),
                    new LimitsClient(),
                    new AggregateMetrics(registry)
            );

    @Test
    void aggregatesBothSourcesInParallel() throws Exception {
        long start = System.nanoTime();

        Aggregate result = controller.aggregate("42");

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertEquals("42", result.user().id());
        assertEquals(new BigDecimal("50000.00"), result.limits().daily());
        assertTrue(elapsed.toMillis() < 2200, "took " + elapsed.toMillis() + " ms");
    }

    @Test
    void failsFastAndCancelsSecondSubtask() {
        long start = System.nanoTime();

        var e = assertThrows(StructuredTaskScope.FailedException.class,
                () -> controller.aggregate("fail"));

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertInstanceOf(IllegalStateException.class, e.getCause());
        assertTrue(elapsed.toMillis() < 1000, "took " + elapsed.toMillis() + " ms");
    }
}
