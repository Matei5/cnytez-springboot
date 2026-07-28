package cnytez.reddit.app.dto;

public record RegisterRequest(
        String name,
        String username,
        String email,
        String password,
        String profilePhoto
) {}
