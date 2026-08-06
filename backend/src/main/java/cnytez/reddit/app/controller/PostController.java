package cnytez.reddit.app.controller;

import cnytez.reddit.app.dto.ApiResponse;
import cnytez.reddit.app.dto.CreatePostRequest;
import cnytez.reddit.app.dto.PostDto;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import cnytez.reddit.app.dto.UpdatePostRequest;
import jakarta.validation.Valid;
import cnytez.reddit.app.dto.ApiMessageResponse;
import cnytez.reddit.app.dto.VoteResponse;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // GET /api/posts
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto>>> getAllPosts(
            @RequestParam(required = false) String subreddit
    ) {
        List<PostDto> posts;
        // todo move the if/else to a method in the postService
        if (subreddit == null || subreddit.isBlank()) {
            posts = postService.getAllPosts();
        } else {
            posts = postService.getPostsBySubreddit(subreddit);
        }

        ApiResponse<List<PostDto>> response = new ApiResponse<>(true, posts);

        return ResponseEntity.ok(response);
    }

    // GET /api/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(@PathVariable UUID id) {
        PostDto post = postService.getPostById(id);
        ApiResponse<PostDto> response = new ApiResponse<>(true, post);

        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDto>> createPost(
            @Valid @ModelAttribute CreatePostRequest request
    ) {
        PostDto post = postService.createPost(request);
        ApiResponse<PostDto> response =
                new ApiResponse<>(true, post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        PostDto post = postService.updatePost(id, request);
        ApiResponse<PostDto> response = new ApiResponse<>(true, post);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> vote(@PathVariable UUID id, @RequestBody VoteRequest request) {
        VoteResponse vote = postService.vote(id, request);
        ApiResponse<VoteResponse> response = new ApiResponse<>(true, vote);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/posts/{id}?requestingUserId={userId}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);

        ApiMessageResponse response = new ApiMessageResponse(
                true,
                "The post was deleted successfully."
        );
        return ResponseEntity.ok(response);
    }
}
