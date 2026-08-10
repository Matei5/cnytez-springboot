package com.cnytez.app.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSubredditRequest(
        @Size(
                min = 3,
                max = 100,
                message = "Display name must contain between 3 and 100 characters."
        )
        String displayName,

        @Size(
                max = 500,
                message = "Description must contain at most 500 characters."
        )
        String description,

        String iconUrl
) {
}