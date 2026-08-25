package com.cnytez.app.moderation;

public interface ContentModerationService {

    ModerationResult moderate(String title, String content);
}
