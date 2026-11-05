package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LimitsClient {

    private static final Logger log = LoggerFactory.getLogger(LimitsClient.class);

    public Limits fetch(String id) throws InterruptedException {
        log.info("fetching limits {} on {}", id, Thread.currentThread());
        Thread.sleep(1500); // имитация медленного внешнего сервиса
        return new Limits(id, new BigDecimal("50000.00"));
    }
}
