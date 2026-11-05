package org.example.payment;

public record Declined(String paymentId, String reason) implements PaymentEvent {
}
