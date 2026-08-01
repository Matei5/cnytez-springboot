package cnytez.reddit.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDto(
        UUID id,
        String title,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        UUID ownerId,
        String author,
        UUID subredditId,
        String subreddit,
        int score,
        int upvotes,
        int downvotes,
        int commentCount
) {}
