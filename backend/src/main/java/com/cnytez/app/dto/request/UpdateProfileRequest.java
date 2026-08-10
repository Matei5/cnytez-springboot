package com.cnytez.app.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        String displayName,

        @Pattern(
                regexp = "^https?://.+$",
                message = "Profile photo URL must use HTTP or HTTPS."
        )
        String profilePhotoURL
) {
}