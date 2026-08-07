package cnytez.reddit.app.dto.response;

import cnytez.reddit.app.dto.AuthUserDto;

public record AuthResponse(
        String accessToken,
        AuthUserDto user
) {
}