package cnytez.reddit.cli.dto;

public record RegisterRequest(
        String name,
        String username,
        String email,
        String password,
        String profilePhoto
) {

}
