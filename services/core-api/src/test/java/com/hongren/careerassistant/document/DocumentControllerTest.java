package com.hongren.careerassistant.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc

class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDocumentReturnCreatedDocument() throws Exception {
        mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "My Resume",
                        "type": "RESUME",
                        "content": "Java Backend Developer"
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("My Resume"))
                .andExpect(jsonPath("$.type").value("RESUME"))
                .andExpect(jsonPath("$.content").value("Java Backend Developer"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void getDocumentReturnsCreatedDocument() throws Exception {
        //创建文档，获取服务端返回的 JSON
        String responseBody = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                "title": "My Resume",
                                "type": "RESUME",
                                "content": "Java Backend Developer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        //从 JSON 中读取新文档的 ID 和创建时间
        JsonNode createdDocument = objectMapper.readTree(responseBody);
        String id = createdDocument.get("id").asText();
        String createdAt = createdDocument.get("createdAt").asText();

        //按 ID 查询，验证返回的是同一份文档
        mockMvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("My Resume"))
                .andExpect(jsonPath("$.type").value("RESUME"))
                .andExpect(jsonPath("$.content").value("Java Backend Developer"))
                .andExpect(jsonPath("$.createdAt").value(createdAt));
    }

    @Test
    void createDocumentWithBlankContentReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "title": "My Resume",
                        "type": "RESUME",
                        "content": "    "
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNonExistentDocumentReturnsNotFound() throws Exception {
        String missingId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/documents/{id}", missingId))
                .andExpect(status().isNotFound());
    }
}
