package org.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GreetingMetrics {
    private final Counter greetings;

    public GreetingMetrics(MeterRegistry registry) {
        this.greetings = Counter.builder("lab.greetings.total")
                .description("Number of greetings served")
                .tag("channel", "api")
                .register(registry);
    }

    public void onGreeting() {
        greetings.increment();
    }
}

