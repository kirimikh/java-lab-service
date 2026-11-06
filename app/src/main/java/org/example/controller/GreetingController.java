package org.example.controller;

import org.example.config.GreetingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class GreetingController {

    private final GreetingProperties properties;
    private static final Logger log = LoggerFactory.getLogger(GreetingController.class);

    GreetingController(GreetingProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/greeting")
    GreetingResponse greeting(@RequestParam(defaultValue = "world") String name) {
        log.info("handled on {}", Thread.currentThread());
        return new GreetingResponse(properties.template().formatted(name));
    }
}

