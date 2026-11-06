package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=18080")
class NoBlockingTest {

    private final WebTestClient client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:18080")
            .responseTimeout(Duration.ofSeconds(5))
            .build();

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    // Для проверки, что BlockHound установился надо встроить этот код в LimitsController
//    return Mono.delay(delay)
//            .map(tick -> {
//        try { Thread.sleep(10); } catch (InterruptedException e) { throw new RuntimeException(e); }
//        return new Limits(id, new BigDecimal("50000.00"));
//    })
//            .doOnCancel(() -> log.info("limits {} request cancelled by client", id));


    @Test
    void aggregateDoesNotBlock() {
        client.get().uri("/api/aggregate/42").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.limits.daily").isEqualTo(50000.00);
    }

    @Test
    void aggregateFallbackDoesNotBlock() {
        client.get().uri("/api/aggregate/slow").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.user.name").isEqualTo("unknown");
    }

    @Test
    void ratesStreamDoesNotBlock() {
        client.get().uri("/api/rates").exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .take(2)
                .collectList()
                .block(Duration.ofSeconds(5)); // block() здесь — в тесте, не в продовом коде
    }
}
