package cnytez.reddit.app.dto;

import java.time.LocalDateTime;

public record SubredditDto(
        Long id,
        String name,
        String photo,
        String banner,
        Long ownerId,
        String ownerUsername,
        LocalDateTime createdAt,
        int memberCount
) {}