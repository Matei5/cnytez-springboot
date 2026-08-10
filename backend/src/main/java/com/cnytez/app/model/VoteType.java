package com.cnytez.app.model;

import lombok.Getter;

@Getter
public enum VoteType {
    UPVOTE("up"),
    DOWNVOTE("down");

    private final String value;

    VoteType(String value) {
        this.value = value;
    }

    public static VoteType fromValue(String value) {
        for (VoteType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid vote type: " + value);
    }
}
