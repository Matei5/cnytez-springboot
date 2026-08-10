package com.cnytez.cli.dto;

public record CreateCommentRequest(
        String title,
        String text,
        String image,
        Long postId,
        Long ownerId,
        Long parentCommentId
) {
}
