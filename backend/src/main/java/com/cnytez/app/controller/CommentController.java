package com.cnytez.app.controller;

import com.cnytez.app.dto.internal.CommentDto;
import com.cnytez.app.dto.request.CreateCommentRequest;
import com.cnytez.app.dto.request.UpdateCommentRequest;
import com.cnytez.app.dto.request.VoteRequest;
import com.cnytez.app.dto.response.ApiListResponse;
import com.cnytez.app.dto.response.ApiMessageResponse;
import com.cnytez.app.dto.response.ApiResponse;
import com.cnytez.app.dto.response.VoteResponse;
import com.cnytez.app.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentDto>> getCommentById(
            @PathVariable UUID id
    ) {
        CommentDto comment = commentService.getCommentById(id);
        ApiResponse<CommentDto> response =
                new ApiResponse<>(true, comment);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiListResponse<CommentDto>> getCommentsByPost(
            @PathVariable UUID postId
    ) {
        List<CommentDto> comments =
                commentService.getCommentsByPost(postId);

        long total = commentService.countCommentsByPost(postId);

        ApiListResponse<CommentDto> response =
                new ApiListResponse<>(true, comments, total);

        return ResponseEntity.ok(response);
    }
    

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentDto comment = commentService.createComment(postId, request);
        ApiResponse<CommentDto> response =
                new ApiResponse<>(true, comment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PutMapping("/comments/{id}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> vote(
            @PathVariable UUID id,
            @RequestBody VoteRequest request
    ) {
        VoteResponse vote = commentService.vote(id, request);
        ApiResponse<VoteResponse> response =
                new ApiResponse<>(true, vote);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiMessageResponse> deleteComment(
            @PathVariable UUID id
    ) {
        commentService.deleteComment(id);

        ApiMessageResponse response = new ApiMessageResponse(
                true,
                "The comment was deleted successfully."
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        CommentDto comment =
                commentService.updateComment(id, request);

        ApiResponse<CommentDto> response =
                new ApiResponse<>(true, comment);

        return ResponseEntity.ok(response);
    }
}
