package cnytez.reddit.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank(message = "Content is required.")
        @Size(
                max = 1000,
                message = "Content must contain at most 1000 characters."
        )
        String content,

        UUID parentId
) {
}