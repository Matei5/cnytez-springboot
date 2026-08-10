package com.cnytez.cli.dto;

public record CreatePostRequest(
        String title,
        String text,
        String image,
        Long subredditId,
        Long ownerId
) {
}
