package cnytez.reddit.app.dto;

public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        String profilePhoto
) {}
