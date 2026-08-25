package com.cnytez.app.moderation;

public record ModerationResult(
        ModerationStatus status,
        String reason
) {
}
