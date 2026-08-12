package com.cnytez.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteUserRequest(
        @NotBlank(message = "Password is required.")
        String password
) {}

