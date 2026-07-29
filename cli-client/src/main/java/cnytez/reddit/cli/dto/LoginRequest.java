package cnytez.reddit.cli.dto;

public record LoginRequest(
        String username,
        String password
) {
}