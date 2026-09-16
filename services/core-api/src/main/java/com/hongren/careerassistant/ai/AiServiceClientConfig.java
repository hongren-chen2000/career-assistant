package com.hongren.careerassistant.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceClientConfig {

    @Bean
    public AiServiceClient aiServiceClient(
            RestClient.Builder restClientBuilder,
            AiServiceProperties properties) {
                
        RestClient restClient = restClientBuilder
                .baseUrl(properties.baseUrl().toString())
                .build();

        return new FastApiAiServiceClient(restClient);
    }
}
