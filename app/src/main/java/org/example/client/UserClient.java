package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    public UserInfo fetch(String id) throws InterruptedException {
        log.info("fetching user {} on {}", id, Thread.currentThread());
        Thread.sleep(1000); // имитация медленного внешнего сервиса
        return new UserInfo(id, "User " + id);
    }
}
