package com.hongren.careerassistant.ai;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "career-assistant.ai-service")
public record AiServiceProperties(
        URI baseUrl, 
        Duration connectTimeout, 
        Duration readTimeout) {
}
