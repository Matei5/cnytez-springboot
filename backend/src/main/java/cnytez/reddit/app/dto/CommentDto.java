package cnytez.reddit.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        String title,
        String text,
        String image,
        LocalDateTime createdAt,
        UUID ownerId,
        String ownerUsername,
        UUID postId,
        UUID parentCommentId,   // null for top-level comments
        int score,
        int upvotes,
        int downvotes,
        int replyCount
) {}
