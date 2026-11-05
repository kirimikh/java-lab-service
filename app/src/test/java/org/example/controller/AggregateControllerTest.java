package org.example.controller;

import org.example.client.LimitsClient;
import org.example.client.UserClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.*;

class AggregateControllerTest {

    private final AggregateController controller =
            new AggregateController(new UserClient(), new LimitsClient());

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
