package com.cnytez.app.e2e;

import com.cnytez.app.AbstractE2ETest;
import com.cnytez.app.dto.request.CreateCommentRequest;
import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.request.VoteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VoteE2ETest extends AbstractE2ETest {

    @Test
    void postAndCommentVotingFlow() throws Exception {
        String authorToken = getAuthToken();
        String voterToken = getAuthToken();

        // 1. author creates subreddit
        String subredditName = "sub_" + UUID.randomUUID().toString().substring(0, 6);
        CreateSubredditRequest createSubRequest = new CreateSubredditRequest(
                subredditName, "Sub Title", "Sub Description", null
        );
        mockMvc.perform(post("/subreddits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubRequest)))
                .andExpect(status().isCreated());

        // 2. author creates post
        MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/posts")
                        .param("title", "Post To Vote On")
                        .param("content", "Voting test content")
                        .param("subreddit", subredditName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> postResponse = objectMapper.readValue(postResult.getResponse().getContentAsString(), Map.class);
        String postId = (String) ((Map<String, Object>) postResponse.get("data")).get("id");

        // 3. voter upvotes post: score = 2 (author +1, voter +1)
        VoteRequest upvoteRequest = new VoteRequest("up");
        mockMvc.perform(put("/posts/" + postId + "/vote")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + voterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upvoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(2))
                .andExpect(jsonPath("$.data.upvotes").value(2))
                .andExpect(jsonPath("$.data.userVote").value("up"));

        // 4. voter switches vote to downvote: score = 0 (author +1, voter -1)
        VoteRequest downvoteRequest = new VoteRequest("down");
        mockMvc.perform(put("/posts/" + postId + "/vote")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + voterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(downvoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(0))
                .andExpect(jsonPath("$.data.downvotes").value(1))
                .andExpect(jsonPath("$.data.userVote").value("down"));

        // 5. voter removes vote (neutral): score = 1 (author +1, voter 0)
        VoteRequest unvoteRequest = new VoteRequest("none");
        mockMvc.perform(put("/posts/" + postId + "/vote")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + voterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unvoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1))
                .andExpect(jsonPath("$.data.upvotes").value(1))
                .andExpect(jsonPath("$.data.downvotes").value(0));

        // 6. author adds comment
        CreateCommentRequest commentRequest = new CreateCommentRequest("Comment to vote on", null);
        MvcResult commentResult = mockMvc.perform(post("/posts/" + postId + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> commentResponse = objectMapper.readValue(commentResult.getResponse().getContentAsString(), Map.class);
        String commentId = (String) ((Map<String, Object>) commentResponse.get("data")).get("id");

        // 7. voter upvotes comment: score = 2 (author +1, voter +1)
        mockMvc.perform(put("/comments/" + commentId + "/vote")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + voterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upvoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(2))
                .andExpect(jsonPath("$.data.userVote").value("up"));
    }
}
