package cnytez.reddit.app.dto;

public record CreatePostRequest(
        String title,
        String content,
        String author,
        String subreddit
) {}