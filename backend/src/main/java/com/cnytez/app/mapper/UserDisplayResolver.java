package com.cnytez.app.mapper;

import com.cnytez.app.model.User;

/*
 * CommentMapper and PostMapper map use this to resolve the username
 * the DTOs are displayed, so they should have [deleted] for user
 * or have the username if the user is still active
 */

public final class UserDisplayResolver {

    public static final String DELETED_USER_LABEL = "[deleted]";

    private UserDisplayResolver() {}

    public static String resolveAuthor(User owner) {
        if (owner == null || owner.getDeletionDate() != null) {
            return DELETED_USER_LABEL;
        }
        return owner.getUsername();
    }
}