package org.example.payment;

import java.math.BigDecimal;

public record Chargeback(String paymentId, BigDecimal amount) implements PaymentEvent {
}
