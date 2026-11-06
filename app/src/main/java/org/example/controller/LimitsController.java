package org.example.controller;

import org.example.client.Limits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@RestController
@RequestMapping("/api")
class LimitsController {

    private static final Logger log = LoggerFactory.getLogger(LimitsController.class);

    @GetMapping("/limits/{id}")
    Mono<Limits> limits(@PathVariable String id) {
        Duration delay = id.equals("slow") ? Duration.ofSeconds(3) : Duration.ofMillis(1500);
        return Mono.delay(delay)
                .map(tick -> new Limits(id, new BigDecimal("50000.00")))
                .doOnCancel(() -> log.info("limits {} request cancelled by client", id));
    }
}
