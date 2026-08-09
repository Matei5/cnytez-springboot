package com.cnytez.app.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        String displayName,

        @Pattern(
                regexp = "^https?://.+$",
                message = "Avatar URL must use HTTP or HTTPS."
        )
        String avatarUrl
) {
}