package cnytez.reddit.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequest(
        @NotBlank(message = "Title is required.")
        @Size(
                min = 3,
                max = 300,
                message = "Title must contain between 3 and 300 characters."
        )
        String title,

        @Size(
                max = 10000,
                message = "Content must contain at most 10000 characters."
        )
        String content,

        @NotBlank(message = "Subreddit is required.")
        String subreddit,

        MultipartFile image,

        Integer filter
) {
}