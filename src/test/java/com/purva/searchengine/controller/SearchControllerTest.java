package com.purva.searchengine.controller;

import com.purva.searchengine.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @Test
    void shouldReturn400whenQueryIsEmpty() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "")
                        .param("threshold", "1.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Query parameter cannot be empty"));
    }

    @Test
    void shouldReturn400whenTopKIsInvalid() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("topK", "-1")
                        .param("threshold", "1.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("topK must be greater than 0"));
    }

    @Test
    void shouldReturn200AndResultsForValidBooleanANDSearch() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("threshold", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn200whenValidRankedSearch() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("topK", "5")
                        .param("threshold", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn400whenThresholdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("threshold", "-0.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Threshold must be in the range (0, 1]"));

        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("threshold", "1.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Threshold must be in the range (0, 1]"));

        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("threshold", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Threshold must be in the range (0, 1]"));
    }

    @Test
    void shouldReturnResultsWithDefaultThreshold() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("topK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}
