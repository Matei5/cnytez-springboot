package cnytez.reddit.app.dto;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String title,
        String text,
        String image,
        LocalDateTime createdAt,
        Long ownerId,
        String ownerUsername,
        Long subredditId,
        String subredditName,
        int score,
        int upvotes,
        int downvotes,
        int commentCount
) {}
