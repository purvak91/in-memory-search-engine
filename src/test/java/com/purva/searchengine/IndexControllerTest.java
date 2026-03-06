package com.purva.searchengine;

import com.purva.searchengine.controller.IndexController;
import com.purva.searchengine.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IndexController.class)
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void shouldReturn201whenIndexing() throws Exception {
        String jsonContent = """
                {
                    "documentId": 1,
                    "content": "This is a test document."
                }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400whenContentIsEmpty() throws Exception {
        String jsonContent = """
                {
                    "documentId": 1,
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Document content cannot be null or blank"));

        jsonContent = """
                {
                    "documentId": 1
                }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Document content cannot be null or blank"));
    }

    @Test
    void shouldReturn400whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void shouldReturn400whenRequestBodyIsInvalid() throws Exception {
        String invalidJson = """
                {
                    "documentId": 1,
                    "content": "This is a test document."
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void shouldReturn400whenDocumentIdIsMissing() throws Exception {
        String jsonContent = """
                {
                    "content": "This is a test document."
                }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }
}
