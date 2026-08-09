package cnytez.reddit.app.dto.response;

public record ErrorDetail(
        String field,
        String message
) {
}