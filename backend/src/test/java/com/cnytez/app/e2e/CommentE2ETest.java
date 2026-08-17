package com.cnytez.app.e2e;

import com.cnytez.app.dto.request.CreateCommentRequest;
import com.cnytez.app.dto.request.CreateSubredditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentE2ETest extends AbstractE2ETest {

    @Test
    void nestedCommentsAndRepliesFlow() throws Exception {
        String authorToken = getAuthToken();
        String userBToken = getAuthToken();
        String userCToken = getAuthToken();

        // 1. author creates subreddit
        String subredditName = "sub_" + UUID.randomUUID().toString().substring(0, 6);
        CreateSubredditRequest createSubRequest = new CreateSubredditRequest(
                subredditName, "Comment Test Sub", "Sub Description", null
        );
        mockMvc.perform(post("/subreddits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubRequest)))
                .andExpect(status().isCreated());

        // 2. author creates post
        MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/posts")
                        .param("title", "Post For Nested Comments")
                        .param("content", "Discussion content")
                        .param("subreddit", subredditName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> postResponse = objectMapper.readValue(postResult.getResponse().getContentAsString(), Map.class);
        String postId = (String) ((Map<String, Object>) postResponse.get("data")).get("id");

        // 3. user b posts top-level comment
        CreateCommentRequest topCommentRequest = new CreateCommentRequest("Top-level comment by User B", null);
        MvcResult topCommentResult = mockMvc.perform(post("/posts/" + postId + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Top-level comment by User B"))
                .andReturn();

        Map<String, Object> topCommentResponse = objectMapper.readValue(topCommentResult.getResponse().getContentAsString(), Map.class);
        String topCommentId = (String) ((Map<String, Object>) topCommentResponse.get("data")).get("id");

        // 4. user c replies to user b's comment
        CreateCommentRequest replyRequest = new CreateCommentRequest(
                "Reply by User C to User B",
                UUID.fromString(topCommentId)
        );
        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userCToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Reply by User C to User B"));

        // 5. fetch all comments for post: verify nested tree structure
        mockMvc.perform(get("/posts/" + postId + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Top-level comment by User B"))
                .andExpect(jsonPath("$.data[0].replies[0].content").value("Reply by User C to User B"));

        // 6. fetch post: verify comment count is 2
        mockMvc.perform(get("/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentCount").value(2));
    }
}
