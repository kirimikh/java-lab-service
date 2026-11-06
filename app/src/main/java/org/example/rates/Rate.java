package org.example.rates;

import java.math.BigDecimal;
import java.time.Instant;

public record Rate(String pair, BigDecimal value, Instant timestamp) {
}
