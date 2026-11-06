package org.example.rates;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RateService {

    private static final BigDecimal BASE = new BigDecimal("90.0000");

    public Rate current() {
        // случайное отклонение от -1.0000 до +1.0000
        var offset = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(-10_000, 10_001), 4);
        return new Rate("USD/RUB", BASE.add(offset), Instant.now());
    }
}
