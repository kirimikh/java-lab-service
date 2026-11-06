package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class LimitsClient {

    private static final Logger log = LoggerFactory.getLogger(LimitsClient.class);

    public Mono<Limits> fetch(String id) {
        Duration delay = id.equals("slow") ? Duration.ofSeconds(3) : Duration.ofMillis(1500);
        return Mono.delay(delay)
                .map(tick -> new Limits(id, new BigDecimal("50000.00")))
                .doOnCancel(() -> log.info("limits {} cancelled", id));
    }
}
