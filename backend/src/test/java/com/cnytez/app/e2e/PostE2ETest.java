package com.cnytez.app.e2e;

import com.cnytez.app.AbstractE2ETest;
import com.cnytez.app.dto.request.CreateSubredditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostE2ETest extends AbstractE2ETest {

    @Test
    void createAndFetchPostFlow() throws Exception {
        String token = getAuthToken();

        // 1. create subreddit
        String subredditName = "testsub_" + UUID.randomUUID().toString().substring(0, 5);
        CreateSubredditRequest createSubredditRequest = new CreateSubredditRequest(
                subredditName,
                "Test Subreddit",
                "Description for test subreddit",
                null
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/subreddits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubredditRequest)))
                .andExpect(status().isCreated());

        // 2. create post (multipart/form-data without file)
        MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/posts")
                        .param("title", "My Awesome Post")
                        .param("content", "This is the content of my awesome post.")
                        .param("subreddit", subredditName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> postResponse = objectMapper.readValue(postResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> postData = (Map<String, Object>) postResponse.get("data");
        String postId = (String) postData.get("id");
        assertThat(postId).isNotNull();

        // 3. fetch post
        MvcResult getPostResult = mockMvc.perform(get("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> getPostResponse = objectMapper.readValue(getPostResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> getPostData = (Map<String, Object>) getPostResponse.get("data");
        assertThat(getPostData.get("title")).isEqualTo("My Awesome Post");
        assertThat(getPostData.get("content")).isEqualTo("This is the content of my awesome post.");
    }
}
