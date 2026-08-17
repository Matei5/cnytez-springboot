package com.cnytez.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAndFetchPost() throws Exception {
        String token = getAuthToken();
        String subName = "testsub_" + java.util.UUID.randomUUID().toString().substring(0, 5);

        // 1. create a subreddit
        com.cnytez.app.dto.request.CreateSubredditRequest subRequest = 
            new com.cnytez.app.dto.request.CreateSubredditRequest(subName, "Test Sub", "Desc", null);
            
        mockMvc.perform(post("/subreddits")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated());

        // 2. create post
        mockMvc.perform(multipart("/posts")
                .param("title", "Integration Test Post")
                .param("content", "This is an integration test content")
                .param("subreddit", subName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Integration Test Post"));
                
        // 3. get posts by subreddit
        mockMvc.perform(get("/posts")
                .param("subreddit", subName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Integration Test Post"));
    }
}
