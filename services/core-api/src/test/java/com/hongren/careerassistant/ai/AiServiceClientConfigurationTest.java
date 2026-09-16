package com.hongren.careerassistant.ai;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "career-assistant.ai-service.base-url=http://localhost:8001"
})
class AiServiceClientConfigurationTest {

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    void registersAiServiceClientBean() {
        Map<String, AiServiceClient> clients
                = applicationContext.getBeansOfType(AiServiceClient.class);

        assertThat(clients).hasSize(1);
        assertThat(clients.values().iterator().next())
                .isInstanceOf(FastApiAiServiceClient.class);
    }
}
