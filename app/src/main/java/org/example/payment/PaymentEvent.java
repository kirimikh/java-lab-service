package org.example.payment;

public sealed interface PaymentEvent permits Authorized, Declined, Refunded {
}

