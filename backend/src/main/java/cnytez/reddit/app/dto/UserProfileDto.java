package cnytez.reddit.app.dto;

public record UserProfileDto(
        String username,
        String email,
        String displayName,
        String profilePhotoURL
) {
}