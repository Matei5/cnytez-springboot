package com.cnytez.app.integration;

import com.cnytez.app.dto.request.CreateSubredditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SubredditIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAndFetchSubreddits() throws Exception {
        String token = getAuthToken();
        String subName = "testsub_" + java.util.UUID.randomUUID().toString().substring(0, 5);

        CreateSubredditRequest request = new CreateSubredditRequest(
                subName,
                "Test Subreddit",
                "A subreddit for testing integration",
                null
        );

        // 1. create subreddit
        mockMvc.perform(post("/subreddits")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(subName));

        // 2. get all subreddits
        mockMvc.perform(get("/subreddits")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
