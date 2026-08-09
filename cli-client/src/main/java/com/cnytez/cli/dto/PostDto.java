package com.cnytez.cli.dto;

public record PostDto(
        Long id,
        String title,
        String text,
        String image,
        String createdAt,
        Long ownerId,
        String ownerUsername,
        Long subredditId,
        String subredditName,
        int score,
        int upvotes,
        int downvotes,
        int commentCount
) {
}
