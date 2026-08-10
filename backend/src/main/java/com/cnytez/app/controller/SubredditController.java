package com.cnytez.app.controller;

import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.internal.SubredditDto;
import com.cnytez.app.service.SubredditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.cnytez.app.dto.response.ApiListResponse;
import com.cnytez.app.dto.response.ApiResponse;
import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.service.PostService;
import com.cnytez.app.dto.response.ApiMessageResponse;
import com.cnytez.app.dto.request.UpdateSubredditRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/subreddits")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;

    @GetMapping
    public ResponseEntity<ApiListResponse<SubredditDto>> getAllSubreddits() {
        List<SubredditDto> subreddits =
                subredditService.getAllSubreddits();

        ApiListResponse<SubredditDto> response =
                new ApiListResponse<>(
                        true,
                        subreddits,
                        subreddits.size()
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<SubredditDto>> getSubredditByName(
            @PathVariable String name
    ) {
        SubredditDto subreddit =
                subredditService.getSubredditByName(name);

        ApiResponse<SubredditDto> response =
                new ApiResponse<>(true, subreddit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}/posts")
    public ResponseEntity<ApiResponse<List<PostDto>>> getSubredditPosts(
            @PathVariable String name
    ) {
        List<PostDto> posts =
                subredditService.getPostsBySubreddit(name);

        ApiResponse<List<PostDto>> response =
                new ApiResponse<>(true, posts);

        return ResponseEntity.ok(response);
    }



    @PostMapping
    public ResponseEntity<ApiResponse<SubredditDto>> createSubreddit(
            @Valid @RequestBody CreateSubredditRequest request
    ) {
        SubredditDto subreddit =
                subredditService.createSubreddit(request);

        ApiResponse<SubredditDto> response =
                new ApiResponse<>(true, subreddit);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{name}")
    public ResponseEntity<ApiResponse<SubredditDto>> updateSubreddit(
            @PathVariable String name,
            @Valid @RequestBody UpdateSubredditRequest request
    ) {
        SubredditDto subreddit =
                subredditService.updateSubreddit(name, request);

        ApiResponse<SubredditDto> response =
                new ApiResponse<>(true, subreddit);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiMessageResponse> deleteSubreddit(
            @PathVariable String name
    ) {
        subredditService.deleteSubreddit(name);

        ApiMessageResponse response = new ApiMessageResponse(
                true,
                "The subreddit was deleted successfully."
        );

        return ResponseEntity.ok(response);
    }

}
