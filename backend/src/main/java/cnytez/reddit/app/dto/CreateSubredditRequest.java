package cnytez.reddit.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSubredditRequest(
        @NotBlank(message = "Name is required.")
        @Size(
                min = 3,
                max = 50,
                message = "Name must contain between 3 and 50 characters."
        )
        @Pattern(
                regexp = "[a-zA-Z0-9_]+",
                message = "Name can contain only letters, numbers and underscores."
        )
        String name,

        @NotBlank(message = "Display name is required.")
        @Size(
                min = 3,
                max = 100,
                message = "Display name must contain between 3 and 100 characters."
        )
        String displayName,

        @NotBlank(message = "Description is required.")
        @Size(
                max = 500,
                message = "Description must contain at most 500 characters."
        )
        String description,

        String iconUrl
) {
}