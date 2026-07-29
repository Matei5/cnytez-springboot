package cnytez.reddit.app.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        String title,
        String text,
        String image,
        LocalDateTime createdAt,
        Long ownerId,
        String ownerUsername,
        Long postId,
        Long parentCommentId,   // null for top-level comments
        int score,
        int upvotes,
        int downvotes,
        int replyCount
) {}
