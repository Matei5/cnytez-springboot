package cnytez.reddit.app.dto;

public record ApiMessageResponse(
        boolean success,
        String message
) {
}