package com.hongren.careerassistant.ai;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.hongren.careerassistant.ai.dto.ExtractFactsRequest;
import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;

public class FastApiAiServiceClient implements AiServiceClient {

    private final RestClient restClient;

    public FastApiAiServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public ExtractFactsResponse extractFacts(String text) {
        return restClient.post()
                .uri("/v1/facts/extract")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExtractFactsRequest(text))
                .retrieve()
                .body(ExtractFactsResponse.class);
    }
}
