package com.cnytez.app.dto.response;

public record AuthResponse(
        String accessToken,
        AuthUserDto user
) {
}