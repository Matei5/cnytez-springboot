package cnytez.reddit.app.response;

public record ApiMessageResponse(
        boolean success,
        String message
) {
}