package cnytez.reddit.app.response;

import java.util.List;

public record ApiListResponse<T>(
        boolean success,
        List<T> data,
        long total
) {
}