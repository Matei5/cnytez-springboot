package cnytez.reddit.app.dto;

import java.util.List;

//todo move to packages
public record ApiListResponse<T>(
        boolean success,
        List<T> data,
        long total
) {
}