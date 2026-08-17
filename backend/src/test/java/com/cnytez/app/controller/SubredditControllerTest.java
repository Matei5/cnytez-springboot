package com.cnytez.app.controller;

import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.dto.internal.SubredditDto;
import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.request.UpdateSubredditRequest;
import com.cnytez.app.service.PostService;
import com.cnytez.app.service.SubredditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubredditController.class)
class SubredditControllerTest extends BaseControllerTest {

    @MockitoBean
    private SubredditService subredditService;

    @MockitoBean
    private PostService postService;

    @Test
    void getAllSubreddits_success() throws Exception {
        SubredditDto subreddit = new SubredditDto(
                UUID.randomUUID(), "news", "News", "Global News",
                100, 5L, null, Instant.now(), null
        );

        when(subredditService.getAllSubreddits()).thenReturn(List.of(subreddit));

        mockMvc.perform(get("/subreddits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("news"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getSubredditByName_success() throws Exception {
        SubredditDto subreddit = new SubredditDto(
                UUID.randomUUID(), "news", "News", "Global News",
                100, 5L, null, Instant.now(), null
        );

        when(subredditService.getSubredditByName("news")).thenReturn(subreddit);

        mockMvc.perform(get("/subreddits/{name}", "news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("news"));
    }

    @Test
    void getSubredditPosts_success() throws Exception {
        PostDto post = new PostDto(
                UUID.randomUUID(), "Post Title", "Content", null, 1,
                "user1", "news", 5, 1, 4, 0, null,
                Instant.now(), Instant.now()
        );

        when(postService.getPostsBySubreddit("news")).thenReturn(List.of(post));

        mockMvc.perform(get("/subreddits/{name}/posts", "news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Post Title"));
    }

    @Test
    void createSubreddit_success() throws Exception {
        CreateSubredditRequest request = new CreateSubredditRequest("news", "News", "Global News", null);
        SubredditDto subreddit = new SubredditDto(
                UUID.randomUUID(), "news", "News", "Global News",
                1, 0L, null, Instant.now(), null
        );

        when(subredditService.createSubreddit(any(CreateSubredditRequest.class))).thenReturn(subreddit);

        mockMvc.perform(post("/subreddits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("news"));
    }

    @Test
    void updateSubreddit_success() throws Exception {
        UpdateSubredditRequest request = new UpdateSubredditRequest("Updated News", "Updated Description", null);
        SubredditDto subreddit = new SubredditDto(
                UUID.randomUUID(), "news", "Updated News", "Updated Description",
                1, 0L, null, Instant.now(), Instant.now()
        );

        when(subredditService.updateSubreddit(eq("news"), any(UpdateSubredditRequest.class))).thenReturn(subreddit);

        mockMvc.perform(put("/subreddits/{name}", "news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Updated News"));
    }

    @Test
    void deleteSubreddit_success() throws Exception {
        doNothing().when(subredditService).deleteSubreddit("news");

        mockMvc.perform(delete("/subreddits/{name}", "news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("The subreddit was deleted successfully."));
    }
}
