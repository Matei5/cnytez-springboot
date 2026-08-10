package com.cnytez.cli.session;

import com.cnytez.cli.dto.UserDto;

public class Session {

    private UserDto currentUser;

    public void login(UserDto user) {
        currentUser = user;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }
}