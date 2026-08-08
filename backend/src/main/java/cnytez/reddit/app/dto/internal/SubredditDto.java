package cnytez.reddit.app.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubredditDto(
        UUID id,
        String name,
        String displayName,
        String description,
        int memberCount,
        long postCount,
        String iconUrl,
        LocalDateTime createdAt
) {
}