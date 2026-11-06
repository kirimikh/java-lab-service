package org.example.controller;

import org.example.config.GreetingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(GreetingController.class)
@EnableConfigurationProperties(GreetingProperties.class)
class GreetingControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void greetsByName() throws Exception {
        webTestClient.get()
                .uri("/api/greeting?name=lab")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, lab!");
    }

    @Test
    void greetsWorldByDefault() throws Exception {
        webTestClient.get()
                .uri("/api/greeting")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, world!");
    }
}
