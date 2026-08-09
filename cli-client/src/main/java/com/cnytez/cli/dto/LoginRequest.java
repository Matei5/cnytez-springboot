package com.cnytez.cli.dto;

public record LoginRequest(
        String username,
        String password
) {
}