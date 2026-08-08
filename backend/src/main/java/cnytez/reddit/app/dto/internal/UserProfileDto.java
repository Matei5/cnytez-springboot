package cnytez.reddit.app.dto.internal;

public record UserProfileDto(
        String username,
        String email,
        String displayName,
        String avatarUrl
) {
}