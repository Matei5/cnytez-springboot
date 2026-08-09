package cnytez.reddit.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 20, message = "Username must contain between 3 and 20 characters.")
        @Pattern(
                regexp = "^[A-Za-z0-9_]+$",
                message = "Username can only contain letters, numbers, and underscores."
        )
        String username,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 100, message = "Email must contain at most 100 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters.")
        String password
) {}
