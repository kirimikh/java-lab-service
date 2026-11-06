package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    public Mono<UserInfo> fetch(String id) {
        if (id.equals("fail")) {
            return Mono.delay(Duration.ofMillis(200)).then(Mono.error(new IllegalStateException(
                    "user service unavailable")));
        }
        return Mono.delay(Duration.ofSeconds(1)).map(tick -> new UserInfo(id, "User " + id));
    }
}
