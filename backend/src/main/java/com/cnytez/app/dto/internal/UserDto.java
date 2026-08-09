package com.cnytez.app.dto.internal;

import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String username,
        String email,
        String profilePhoto
) {}
