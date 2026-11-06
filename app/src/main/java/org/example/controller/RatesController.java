package org.example.controller;

import org.example.rates.Rate;
import org.example.rates.RateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api")
class RatesController {

    private final RateService rateService;

    RatesController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping(value = "/rates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Rate> rates() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> rateService.current());
    }
}
