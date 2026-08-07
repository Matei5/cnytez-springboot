package cnytez.reddit.app.response;

import java.util.List;

public record ApiError(
        String code,
        String message,
        List<ErrorDetail> details
) {
}