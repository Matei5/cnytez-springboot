package cnytez.reddit.app.controller;

import cnytez.reddit.app.dto.CommentDto;
import cnytez.reddit.app.dto.CreateCommentRequest;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // GET /api/comments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    // GET /api/comments/by-post/{postId} top-level comments only
    @GetMapping("/by-post/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsByPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    // GET /api/comments/{id}/replies
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getReplies(id));
    }

    // GET /api/comments/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId));
    }

    // POST /api/comments
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(request));
    }

    // POST /api/comments/{id}/vote
    @PostMapping("/{id}/vote")
    public ResponseEntity<CommentDto> vote(@PathVariable UUID id, @RequestBody VoteRequest request) {
        return ResponseEntity.ok(commentService.vote(id, request));
    }

    // DELETE /api/comments/{id}?requestingUserId={userId}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id,
                                              @RequestParam UUID requestingUserId) {
        commentService.deleteComment(id, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}
