package cnytez.reddit.app.dto;

import java.time.Instant;

public record ApiErrorResponse(
        boolean success,
        ApiError error,
        Instant timestamp,
        String path
) {
}