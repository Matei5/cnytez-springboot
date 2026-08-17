package com.cnytez.app.controller;

import com.cnytez.app.dto.internal.CommentDto;
import com.cnytez.app.dto.request.CreateCommentRequest;
import com.cnytez.app.dto.request.UpdateCommentRequest;
import com.cnytez.app.dto.request.VoteRequest;
import com.cnytez.app.dto.response.VoteResponse;
import com.cnytez.app.service.CommentService;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest extends BaseControllerTest {

    @MockitoBean
    private CommentService commentService;

    @Test
    void getCommentById_success() throws Exception {
        UUID id = UUID.randomUUID();
        CommentDto mockComment = new CommentDto(
                id, UUID.randomUUID(), null, "Test comment", "user1",
                10, 2, 8, null, Instant.now(), Instant.now(), List.of()
        );

        when(commentService.getCommentById(id)).thenReturn(mockComment);

        mockMvc.perform(get("/comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Test comment"));
    }

    @Test
    void getCommentsByPost_success() throws Exception {
        UUID postId = UUID.randomUUID();
        CommentDto mockComment = new CommentDto(
                UUID.randomUUID(), postId, null, "Test comment", "user1",
                10, 2, 8, null, Instant.now(), Instant.now(), List.of()
        );

        when(commentService.getCommentsByPost(postId)).thenReturn(List.of(mockComment));
        when(commentService.countCommentsByPost(postId)).thenReturn(1L);

        mockMvc.perform(get("/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].content").value("Test comment"));
    }

    @Test
    void createComment_success() throws Exception {
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest("New comment", null);

        CommentDto mockComment = new CommentDto(
                UUID.randomUUID(), postId, null, "New comment", "user1",
                0, 0, 0, null, Instant.now(), Instant.now(), List.of()
        );

        when(commentService.createComment(eq(postId), any(CreateCommentRequest.class))).thenReturn(mockComment);

        mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("New comment"));
    }

    @Test
    void updateComment_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCommentRequest request = new UpdateCommentRequest("Updated comment");

        CommentDto mockComment = new CommentDto(
                id, UUID.randomUUID(), null, "Updated comment", "user1",
                0, 0, 0, null, Instant.now(), Instant.now(), List.of()
        );

        when(commentService.updateComment(eq(id), any(UpdateCommentRequest.class))).thenReturn(mockComment);

        mockMvc.perform(put("/comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Updated comment"));
    }

    @Test
    void vote_success() throws Exception {
        UUID id = UUID.randomUUID();
        VoteRequest request = new VoteRequest("upvote");
        VoteResponse response = new VoteResponse(1, 0, 1, "upvote");

        when(commentService.vote(eq(id), any(VoteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/comments/{id}/vote", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(1));
    }

    @Test
    void deleteComment_success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(commentService).deleteComment(id);

        mockMvc.perform(delete("/comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("The comment was deleted successfully."));
    }
}
