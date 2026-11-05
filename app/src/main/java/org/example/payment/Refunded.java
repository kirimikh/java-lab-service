package org.example.payment;

import java.math.BigDecimal;

public record Refunded(String paymentId, BigDecimal amount) implements PaymentEvent {
}
