package com.cnytez.app.dto.internal;

import java.time.Instant;
import java.util.UUID;

public record SubredditDto(
        UUID id,
        String name,
        String displayName,
        String description,
        int memberCount,
        long postCount,
        String iconUrl,
        Instant createdAt
) {
}