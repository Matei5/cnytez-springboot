package cnytez.reddit.app.dto;

public record CreateCommentRequest(
        String title,
        String text,
        String image,
        Long postId,
        Long ownerId,
        Long parentCommentId    // null for top-level comments
) {}
