package cnytez.reddit.app.dto;

import cnytez.reddit.app.model.VoteType;

import java.util.UUID;

public record VoteRequest(
        UUID userId,
        VoteType voteType
) {}
