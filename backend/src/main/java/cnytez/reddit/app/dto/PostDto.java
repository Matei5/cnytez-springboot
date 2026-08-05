package cnytez.reddit.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDto(
        UUID id,
        String title,
        String content,
        String imageUrl,
        Integer filter,
        String author,
        String subreddit,
        int upvotes,
        int downvotes,
        int score,
        int commentCount,
        String userVote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}