package org.example.payment;

public class PaymentEventDescriber {
    public String describe(PaymentEvent event) {
        return switch (event) {
            case Authorized(var id, var amount) -> "authorized %s: %s".formatted(id, amount);
            case Declined(var id, var reason) -> "declined %s: %s".formatted(id, reason);
            case Refunded(var id, var amount) -> "refunded %s: %s".formatted(id, amount);
        };
    }

}
