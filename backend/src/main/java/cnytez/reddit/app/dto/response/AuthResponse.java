package cnytez.reddit.app.dto.response;

public record AuthResponse(
        String accessToken,
        AuthUserDto user
) {
}