package cnytez.reddit.cli.dto;

public record VoteRequest(
        Long userId,
        VoteType voteType
) {
}
