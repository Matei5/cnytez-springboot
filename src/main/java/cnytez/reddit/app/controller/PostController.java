package cnytez.reddit.app.controller;

import cnytez.reddit.app.dto.CreatePostRequest;
import cnytez.reddit.app.dto.PostDto;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // GET /api/posts
    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // GET /api/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // GET /api/posts/by-subreddit/{subredditId}
    @GetMapping("/by-subreddit/{subredditId}")
    public ResponseEntity<List<PostDto>> getPostsBySubreddit(@PathVariable Long subredditId) {
        return ResponseEntity.ok(postService.getPostsBySubreddit(subredditId));
    }

    // GET /api/posts/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<PostDto>> getPostsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    // POST /api/posts/{id}/vote
    @PostMapping("/{id}/vote")
    public ResponseEntity<PostDto> vote(@PathVariable Long id, @RequestBody VoteRequest request) {
        return ResponseEntity.ok(postService.vote(id, request));
    }

    // DELETE /api/posts/{id}?requestingUserId={userId}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @RequestParam Long requestingUserId) {
        postService.deletePost(id, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}
