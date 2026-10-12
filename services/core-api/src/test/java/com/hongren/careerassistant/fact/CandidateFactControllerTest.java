package com.hongren.careerassistant.fact;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongren.careerassistant.ai.AiServiceClient;
import com.hongren.careerassistant.ai.AiServiceClientException;
import com.hongren.careerassistant.ai.dto.CandidateFactResponse;
import com.hongren.careerassistant.ai.dto.EvidenceSpanResponse;
import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;

@SpringBootTest
@AutoConfigureMockMvc
class CandidateFactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 替换 Spring 容器中的 AI 客户端，保留真实 Controller 和业务服务。
    @MockBean
    private AiServiceClient aiServiceClient;

    @Test
    void extractsFactsAndReturnsSameFactsWhenQueried() throws Exception {
        String originalText = "Java backend experience";

        CandidateFactResponse aiFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(originalText, 0, 23),
                0.5,
                "pending_review");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock", List.of(aiFact)));

        String documentBody = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "title": "My Resume",
                                  "type": "RESUME",
                                  "content": "Java backend experience"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String documentId = objectMapper.readTree(documentBody)
                .get("id")
                .asText();

        String extractedBody = mockMvc.perform(post(
                "/api/documents/{id}/facts/extract", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].documentId").value(documentId))
                .andExpect(jsonPath("$[0].factType").value("EXPERIENCE"))
                .andExpect(jsonPath("$[0].value").value(originalText))
                .andExpect(jsonPath("$[0].evidence.sourceText")
                        .value(originalText))
                .andExpect(jsonPath("$[0].evidence.start").value(0))
                .andExpect(jsonPath("$[0].evidence.end").value(23))
                .andExpect(jsonPath("$[0].confidence").value(0.5))
                .andExpect(jsonPath("$[0].status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String queriedBody = mockMvc.perform(get(
                "/api/documents/{id}/facts", documentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 比较完整 JSON，确保查询没有重新生成事实 ID 或创建时间。
        assertThat(objectMapper.readTree(queriedBody))
                .isEqualTo(objectMapper.readTree(extractedBody));

        verify(aiServiceClient).extractFacts(originalText);
    }

    @Test
    void extractingFactsForMissingDocumentReturnsNotFound() throws Exception {
        UUID missingDocumentId = UUID.randomUUID();

        mockMvc.perform(post(
                "/api/documents/{id}/facts/extract",
                missingDocumentId))
                .andExpect(status().isNotFound());

        // 文档不存在时，应在调用 AI 之前结束请求。
        verifyNoInteractions(aiServiceClient);
    }

    @Test
    void queryingFactsForMissingDocumentReturnsNotFound() throws Exception {
        UUID missingDocumentId = UUID.randomUUID();

        mockMvc.perform(get(
                "/api/documents/{id}/facts",
                missingDocumentId))
                .andExpect(status().isNotFound());

        verifyNoInteractions(aiServiceClient);
    }

    @Test
    void queryingFactsBeforeExtractionReturnsEmptyArray() throws Exception {
        String documentBody = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                              "title": "New Resume",
                              "type": "RESUME",
                              "content": "Java backend experience"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String documentId = objectMapper.readTree(documentBody)
                .get("id")
                .asText();

        // GET 只查询已保存的事实，不应自动触发提取。
        mockMvc.perform(get("/api/documents/{id}/facts", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verifyNoInteractions(aiServiceClient);
    }

    @Test
    void aiServiceFailureReturnsSafeBadGatewayResponse() throws Exception {
        String originalText = "Java backend experience";

        String documentBody = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                              "title": "My Resume",
                              "type": "RESUME",
                              "content": "Java backend experience"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String documentId = objectMapper.readTree(documentBody)
                .get("id")
                .asText();

        // 故意提供带内部细节的异常，验证 HTTP 响应不会直接暴露它。
        when(aiServiceClient.extractFacts(originalText))
                .thenThrow(new AiServiceClientException(
                        "Sensitive provider error details"));

        String errorBody = mockMvc.perform(post(
                "/api/documents/{id}/facts/extract", documentId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.title")
                        .value("AI service request failed"))
                .andExpect(jsonPath("$.detail")
                        .value("Unable to extract facts. Please try again later."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(errorBody)
                .doesNotContain("Sensitive provider error details")
                .doesNotContain(originalText);

        // 提取失败不能留下候选事实，查询也不能再次触发 AI 调用。
        mockMvc.perform(get("/api/documents/{id}/facts", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(aiServiceClient).extractFacts(originalText);
    }
}
