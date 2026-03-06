package com.purva.searchengine;

import com.purva.searchengine.controller.SearchController;
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
        mockMvc.perform(get("/api/search").param("query", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Query parameter cannot be empty"));
    }

    @Test
    void shouldReturn400whenTopKIsInvalid() throws Exception {
        mockMvc.perform(get("/api/search").param("query", "test").param("topK", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("topK must be greater than 0"));
    }

    @Test
    void shouldReturn200whenValidSearch() throws Exception {
        mockMvc.perform(get("/api/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn200whenValidRankedSearch() throws Exception {
        mockMvc.perform(get("/api/search").param("query", "test").param("topK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}
