package cnytez.reddit.app.dto;

import java.util.UUID;

public record CreateCommentRequest(
        String title,
        String text,
        String image,
        UUID postId,
        UUID ownerId,
        UUID parentCommentId    // null for top-level comments
) {}
