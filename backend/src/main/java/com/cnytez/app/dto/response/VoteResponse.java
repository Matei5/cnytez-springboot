package com.cnytez.app.dto.response;

public record VoteResponse(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}