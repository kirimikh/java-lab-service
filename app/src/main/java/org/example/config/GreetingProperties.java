package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lab.greeting")
public record GreetingProperties(String template, boolean signed) {
}
