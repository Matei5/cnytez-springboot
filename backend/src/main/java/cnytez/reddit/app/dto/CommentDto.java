
package cnytez.reddit.app.dto;

import java.time.LocalDateTime;
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentDto> replies
) {
}