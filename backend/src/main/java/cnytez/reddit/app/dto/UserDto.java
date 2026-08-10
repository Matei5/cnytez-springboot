package cnytez.reddit.app.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String username,
        String email,
        String profilePhotoURL
) {}
