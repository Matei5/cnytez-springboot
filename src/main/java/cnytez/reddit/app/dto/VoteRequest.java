package cnytez.reddit.app.dto;

import cnytez.reddit.app.model.VoteType;

public record VoteRequest(
        Long userId,
        VoteType voteType
) {}
