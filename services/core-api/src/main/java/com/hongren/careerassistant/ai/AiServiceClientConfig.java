package com.hongren.careerassistant.ai;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceClientConfig {

    @Bean
    public AiServiceClient aiServiceClient(
            RestClient.Builder restClientBuilder,
            AiServiceProperties properties,
            @Qualifier ("aiServiceRequestFactory") 
            ClientHttpRequestFactory requestFactory) {

        RestClient restClient = restClientBuilder
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();

        return new FastApiAiServiceClient(restClient);
    }

    @Bean
    public ClientHttpRequestFactory aiServiceRequestFactory(
            AiServiceProperties properties) {

        //该请求工厂只负责 Spring Boot 到 AI Service 的 HTTP 调用。
        SimpleClientHttpRequestFactory requestFactory
                = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                properties.connectTimeout());

        requestFactory.setReadTimeout(
                properties.readTimeout());

        return requestFactory;
    }
}
