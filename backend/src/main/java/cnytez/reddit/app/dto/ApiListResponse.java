package cnytez.reddit.app.dto;

import java.util.List;

public record ApiListResponse<T>(
        boolean success,
        List<T> data,
        long total
) {
}