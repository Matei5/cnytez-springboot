
package cnytez.reddit.app.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID postId,
        UUID parentId,
        String content,
        String author,
        int upvotes,
        int downvotes,
        int score,
        String userVote,
        Instant createdAt,
        Instant updatedAt,
        List<CommentDto> replies
) {
}