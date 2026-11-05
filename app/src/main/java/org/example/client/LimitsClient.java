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
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            log.info("limits {} cancelled", id);
            throw e;
        }
        return new Limits(id, new BigDecimal("50000.00"));
    }
}
