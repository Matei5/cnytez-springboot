package cnytez.reddit.app.dto;

public record AuthResponse(
        String accessToken,
        AuthUserDto user
) {
}