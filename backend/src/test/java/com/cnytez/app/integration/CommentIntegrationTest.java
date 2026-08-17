package com.cnytez.app.integration;

import com.cnytez.app.dto.request.CreateCommentRequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAndFetchComments() throws Exception {
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
        String postResponse = mockMvc.perform(multipart("/posts")
                .param("title", "Integration Test Post")
                .param("content", "This is an integration test content")
                .param("subreddit", subName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String postId = com.jayway.jsonpath.JsonPath.read(postResponse, "$.data.id");

        // 3. create comment
        CreateCommentRequest commentRequest = new CreateCommentRequest("This is a test comment", null);

        mockMvc.perform(post("/posts/" + postId + "/comments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("This is a test comment"));

        // 4. get comments by post
        mockMvc.perform(get("/posts/" + postId + "/comments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("This is a test comment"));
    }
}
