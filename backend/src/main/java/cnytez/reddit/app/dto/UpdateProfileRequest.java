package cnytez.reddit.app.dto;

public record UpdateProfileRequest(
        String name,
        String username,
        String email,
        String profilePhoto
) {}
