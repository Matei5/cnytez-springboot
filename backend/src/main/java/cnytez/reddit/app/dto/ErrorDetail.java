package cnytez.reddit.app.dto;

public record ErrorDetail(
        String field,
        String message
) {
}