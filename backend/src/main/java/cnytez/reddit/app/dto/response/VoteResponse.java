package cnytez.reddit.app.dto.response;

public record VoteResponse(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}