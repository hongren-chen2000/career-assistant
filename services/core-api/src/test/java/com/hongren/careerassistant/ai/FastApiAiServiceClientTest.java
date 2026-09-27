package com.hongren.careerassistant.ai;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import static org.springframework.http.HttpMethod.POST;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import com.hongren.careerassistant.ai.dto.CandidateFactResponse;
import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;

class FastApiAiServiceClientTest {

    @Test
    void extractFactsPostsTextAndReturnsCandidateFacts() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://localhost:8001");

        MockRestServiceServer server
                = MockRestServiceServer.bindTo(restClientBuilder).build();

        AiServiceClient client
                = new FastApiAiServiceClient(restClientBuilder.build());

        server.expect(requestTo(
                "http://localhost:8001/v1/facts/extract"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"text":"Built a Spring Boot API"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "mode": "mock",
                          "facts": [
                            {
                              "fact_type": "experience",
                              "value": "Built a Spring Boot API",
                              "evidence": {
                                "source_text": "Built a Spring Boot API",
                                "start": 0,
                                "end": 23
                              },
                              "confidence": 0.5,
                              "status": "pending_review"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        ExtractFactsResponse response
                = client.extractFacts("Built a Spring Boot API");

        assertThat(response.mode()).isEqualTo("mock");
        assertThat(response.facts()).hasSize(1);

        CandidateFactResponse fact = response.facts().getFirst();

        assertThat(fact.factType()).isEqualTo("experience");
        assertThat(fact.value()).isEqualTo("Built a Spring Boot API");
        assertThat(fact.evidence().sourceText())
                .isEqualTo("Built a Spring Boot API");
        assertThat(fact.evidence().start()).isZero();
        assertThat(fact.evidence().end()).isEqualTo(23);
        assertThat(fact.confidence()).isEqualTo(0.5);
        assertThat(fact.status()).isEqualTo("pending_review");

        server.verify();
    }

    @Test
    void extractFactsThrowsSafeExceptionWhenAiServiceReturnsServerError() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://localhost:8001");

        MockRestServiceServer server
                = MockRestServiceServer.bindTo(restClientBuilder).build();

        AiServiceClient client
                = new FastApiAiServiceClient(restClientBuilder.build());

        server.expect(requestTo(
                "http://localhost:8001/v1/facts/extract"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Sensitive provider error details"));

        assertThatThrownBy(()
                -> client.extractFacts("Private resume content"))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service request failed")
                .hasMessageNotContaining("Private resume content")
                .hasMessageNotContaining("Sensitive provider error details");

        server.verify();
    }

    @Test
    void extractFactsThrowsSafeExceptionWhenAiServiceIsUnavailable() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://localhost:8001");

        MockRestServiceServer server
                = MockRestServiceServer.bindTo(restClientBuilder).build();

        AiServiceClient client
                = new FastApiAiServiceClient(restClientBuilder.build());

        server.expect(requestTo(
                "http://localhost:8001/v1/facts/extract"))
                .andExpect(method(POST))
                .andRespond(withException(
                        new IOException("Connection refused")));

        assertThatThrownBy(()
                -> client.extractFacts("Private resume content"))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service request failed")
                .hasMessageNotContaining("Private resume content")
                .hasMessageNotContaining("Connection refused");

        server.verify();
    }
}
