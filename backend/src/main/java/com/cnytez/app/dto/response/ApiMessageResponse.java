package com.cnytez.app.dto.response;

public record ApiMessageResponse(
        boolean success,
        String message
) {
}