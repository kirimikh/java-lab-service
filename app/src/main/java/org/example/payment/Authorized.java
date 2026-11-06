package org.example.payment;

import java.math.BigDecimal;

public record Authorized(String paymentId, BigDecimal amount) implements PaymentEvent {
}