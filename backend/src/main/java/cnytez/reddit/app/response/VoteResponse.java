package cnytez.reddit.app.response;

public record VoteResponse(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}