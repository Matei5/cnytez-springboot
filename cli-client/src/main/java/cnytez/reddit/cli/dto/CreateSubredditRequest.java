package cnytez.reddit.cli.dto;

public record CreateSubredditRequest(
        String name,
        String photo,
        String banner,
        Long ownerId
) {
}
