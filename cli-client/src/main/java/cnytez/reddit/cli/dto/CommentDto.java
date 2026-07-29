package cnytez.reddit.cli.dto;

public record CommentDto(
        Long id,
        String title,
        String text,
        String image,
        String createdAt,
        Long ownerId,
        String ownerUsername,
        Long postId,
        Long parentCommentId,
        int score,
        int upvotes,
        int downvotes
) {
}
