package cnytez.reddit.app.response;

import cnytez.reddit.app.dto.AuthUserDto;

public record AuthResponse(
        String accessToken,
        AuthUserDto user
) {
}