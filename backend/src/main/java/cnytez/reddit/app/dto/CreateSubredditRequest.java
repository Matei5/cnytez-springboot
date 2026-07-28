package cnytez.reddit.app.dto;

public record CreateSubredditRequest(
        String name,
        String photo,
        String banner,
        Long ownerId
) {}
