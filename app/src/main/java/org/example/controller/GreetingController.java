package org.example.controller;

import org.example.config.GreetingProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class GreetingController {

    private final GreetingProperties properties;

    GreetingController(GreetingProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/greeting")
    GreetingResponse greeting(@RequestParam(defaultValue = "world") String name) {
        return new GreetingResponse(properties.template().formatted(name));
    }
}

