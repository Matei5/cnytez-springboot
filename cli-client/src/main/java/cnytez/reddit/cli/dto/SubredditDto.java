package cnytez.reddit.cli.dto;

public record SubredditDto(
        Long id,
        String name,
        String photo,
        String banner,
        Long ownerId,
        String ownerUsername,
        String createdAt,
        int memberCount
) {
}
