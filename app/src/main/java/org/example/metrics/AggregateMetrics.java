package org.example.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AggregateMetrics {
    private final MeterRegistry registry;
    private final Timer duration;

    public AggregateMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.duration = Timer.builder("lab.aggregate.duration")
                .description("Duration of aggregation")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public Timer.Sample start() {
        return Timer.start(registry);
    }

    public long stop(Timer.Sample sample) {
        return sample.stop(duration);
    }
}

