package com.cnytez.app.dto.internal;

import java.time.Instant;
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
        Instant createdAt,
        Instant updatedAt
) {
}