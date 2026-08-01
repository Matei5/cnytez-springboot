package cnytez.reddit.app.dto;

import java.util.UUID;

public record CreateSubredditRequest(
        String name,
        String photo,
        String banner,
        UUID ownerId
) {}
