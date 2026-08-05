package cnytez.reddit.app.dto;

public record VoteResponse(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}