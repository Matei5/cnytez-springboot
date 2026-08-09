package com.cnytez.app.dto.response;

public record ErrorDetail(
        String field,
        String message
) {
}