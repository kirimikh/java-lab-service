package org.example.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentEventDescriberTest {

    private final PaymentEventDescriber describer = new PaymentEventDescriber();

    @Test
    void describesAuthorized() {
        assertEquals("authorized p-1: 100.00",
                describer.describe(new Authorized("p-1", new BigDecimal("100.00"))));
    }

    @Test
    void describesDeclined() {
        assertEquals("declined p-2: insufficient funds",
                describer.describe(new Declined("p-2", "insufficient funds")));
    }

    @Test
    void describesRefunded() {
        assertEquals("refunded p-3: 25.50",
                describer.describe(new Refunded("p-3", new BigDecimal("25.50"))));
    }

    @Test
    void describesChargeback() {
        assertEquals("chargeback p-4: 100.00",
                describer.describe(new Chargeback("p-4", new BigDecimal("100.00"))));
    }
}
