package com.hongren.careerassistant.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

class DocumentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

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
}
