package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lab.limits")
public record LimitsClientProperties(String baseUrl) {
}
