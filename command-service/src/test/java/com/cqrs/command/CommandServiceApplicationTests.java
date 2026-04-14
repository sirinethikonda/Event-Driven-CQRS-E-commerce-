package com.cqrs.command;

import com.cqrs.command.config.TestKafkaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestKafkaConfig.class)
class CommandServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
