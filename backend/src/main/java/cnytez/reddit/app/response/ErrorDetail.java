package cnytez.reddit.app.response;

public record ErrorDetail(
        String field,
        String message
) {
}