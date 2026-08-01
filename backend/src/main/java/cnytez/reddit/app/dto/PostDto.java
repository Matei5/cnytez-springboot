package cnytez.reddit.app.dto;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String title,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        Long ownerId,
        String author,
        Long subredditId,
        String subreddit,
        int score,
        int upvotes,
        int downvotes,
        int commentCount
) {}
