package com.hongren.careerassistant.ai;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(properties = {
    "career-assistant.ai-service.base-url=http://localhost:8001",
    "career-assistant.ai-service.connect-timeout=2s",
    "career-assistant.ai-service.read-timeout=30s"

})
class AiServiceClientConfigurationTest {

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Autowired
    private AiServiceProperties aiServiceProperties;

    @Autowired
    @Qualifier("aiServiceRequestFactory")
    private ClientHttpRequestFactory requestFactory;

    @Test
    void registersAiServiceClientBean() {
        Map<String, AiServiceClient> clients
                = applicationContext.getBeansOfType(AiServiceClient.class);

        assertThat(clients).hasSize(1);
        assertThat(clients.values().iterator().next())
                .isInstanceOf(FastApiAiServiceClient.class);
    }

    @Test
    void bindAiServiceTimeoutProperties() {
        assertThat(aiServiceProperties.connectTimeout())
                .isEqualTo(Duration.ofSeconds(2));

        assertThat(aiServiceProperties.readTimeout())
                .isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void createRequestFactoryWithConfiguredTimeouts() {
        assertThat(requestFactory)
                .isInstanceOf(SimpleClientHttpRequestFactory.class);

        assertThat(ReflectionTestUtils.getField(
                requestFactory, "connectTimeout"))
                .isEqualTo(2000);

        assertThat(ReflectionTestUtils.getField(
                requestFactory, "readTimeout"))
                .isEqualTo(30000);
    }
}
