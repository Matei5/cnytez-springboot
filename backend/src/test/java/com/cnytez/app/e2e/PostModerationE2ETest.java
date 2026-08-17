package com.cnytez.app.e2e;

import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.request.UpdatePostRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostModerationE2ETest extends AbstractE2ETest {

    @Test
    void postEditDeleteAndAuthorizationFlow() throws Exception {
        String authorToken = getAuthToken();
        String unauthorizedUserToken = getAuthToken();

        // 1. author creates subreddit
        String subredditName = "sub_" + UUID.randomUUID().toString().substring(0, 6);
        CreateSubredditRequest createSubRequest = new CreateSubredditRequest(
                subredditName, "Moderation Test Sub", "Sub Description", null
        );
        mockMvc.perform(post("/subreddits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubRequest)))
                .andExpect(status().isCreated());

        // 2. author creates post
        MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/posts")
                        .param("title", "Original Post Title")
                        .param("content", "Original content")
                        .param("subreddit", subredditName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> postResponse = objectMapper.readValue(postResult.getResponse().getContentAsString(), Map.class);
        String postId = (String) ((Map<String, Object>) postResponse.get("data")).get("id");

        // 3. unauthorized user attempts to edit post: 403 forbidden
        UpdatePostRequest unauthorizedEditRequest = new UpdatePostRequest("Hacked Title", "Hacked Content");
        mockMvc.perform(put("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + unauthorizedUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unauthorizedEditRequest)))
                .andExpect(status().isForbidden());

        // 4. unauthorized user attempts to delete post: 403 forbidden
        mockMvc.perform(delete("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + unauthorizedUserToken))
                .andExpect(status().isForbidden());

        // 5. author successfully updates post: 200 ok
        UpdatePostRequest validEditRequest = new UpdatePostRequest("Updated Title By Author", "Updated Content By Author");
        mockMvc.perform(put("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title By Author"))
                .andExpect(jsonPath("$.data.content").value("Updated Content By Author"));

        // 6. fetch post: verify updated state
        mockMvc.perform(get("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title By Author"));

        // 7. author deletes post: 200 ok
        mockMvc.perform(delete("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. fetch deleted post: title reflects soft deletion
        mockMvc.perform(get("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("[deleted by user]"));
    }
}
