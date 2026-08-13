package com.cnytez.app.controller;

import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.dto.request.CreatePostRequest;
import com.cnytez.app.dto.request.UpdatePostRequest;
import com.cnytez.app.dto.request.VoteRequest;
import com.cnytez.app.dto.response.VoteResponse;
import com.cnytez.app.service.JwtService;
import com.cnytez.app.service.PostService;
import com.cnytez.app.logging.LogManager;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private LogManager logManager;

    @MockitoBean
    private PostService postService;

    @Test
    void getAllPosts_success() throws Exception {
        PostDto post = new PostDto(
                UUID.randomUUID(), "Title", "Content", null, 1,
                "user1", "subreddit1", 5, 1, 4, 0, null,
                Instant.now(), Instant.now()
        );

        when(postService.getAllPostsOrGetPostsBySubreddit(null)).thenReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Title"));
    }

    @Test
    void getPostById_success() throws Exception {
        UUID id = UUID.randomUUID();
        PostDto post = new PostDto(
                id, "Title", "Content", null, 1,
                "user1", "subreddit1", 5, 1, 4, 0, null,
                Instant.now(), Instant.now()
        );

        when(postService.getPostById(id)).thenReturn(post);

        mockMvc.perform(get("/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    void createPost_success() throws Exception {
        PostDto post = new PostDto(
                UUID.randomUUID(), "New Title", "New Content", null, 1,
                "user1", "subreddit1", 0, 0, 0, 0, null,
                Instant.now(), Instant.now()
        );

        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(post);

        mockMvc.perform(multipart("/posts")
                        .param("title", "New Title")
                        .param("content", "New Content")
                        .param("subreddit", "subreddit1")
                        .param("filter", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Title"));
    }

    @Test
    void updatePost_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePostRequest request = new UpdatePostRequest("Updated Title", "Updated Content");
        PostDto post = new PostDto(
                id, "Updated Title", "Updated Content", null, 1,
                "user1", "subreddit1", 0, 0, 0, 0, null,
                Instant.now(), Instant.now()
        );

        when(postService.updatePost(eq(id), any(UpdatePostRequest.class))).thenReturn(post);

        mockMvc.perform(put("/posts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void vote_success() throws Exception {
        UUID id = UUID.randomUUID();
        VoteRequest request = new VoteRequest("upvote");
        VoteResponse response = new VoteResponse(1, 0, 1, "upvote");

        when(postService.vote(eq(id), any(VoteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/posts/{id}/vote", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1));
    }

    @Test
    void deletePost_success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(postService).deletePost(id);

        mockMvc.perform(delete("/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("The post was deleted successfully."));
    }
}
