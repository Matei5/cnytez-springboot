package cnytez.reddit.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubredditDto(
        UUID id,
        String name,
        String photo,
        String banner,
        UUID ownerId,
        String ownerUsername,
        LocalDateTime createdAt,
        int memberCount
) {}
