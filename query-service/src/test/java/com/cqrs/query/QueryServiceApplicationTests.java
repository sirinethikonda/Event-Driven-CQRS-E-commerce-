package com.cqrs.query;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.kafka.streams.application-id=test-app",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class QueryServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
