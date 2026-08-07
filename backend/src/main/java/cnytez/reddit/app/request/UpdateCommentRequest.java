package cnytez.reddit.app.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
        @NotBlank(message = "Content is required.")
        @Size(
                max = 1000,
                message = "Content must contain at most 1000 characters."
        )
        String content
) {
}