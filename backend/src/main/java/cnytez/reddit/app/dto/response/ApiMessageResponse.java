package cnytez.reddit.app.dto.response;

public record ApiMessageResponse(
        boolean success,
        String message
) {
}