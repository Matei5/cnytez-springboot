package com.cnytez.cli.dto;

public record VoteRequest(
        Long userId,
        VoteType voteType
) {
}
