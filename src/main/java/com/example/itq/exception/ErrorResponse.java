package com.example.itq.exception;

public record ErrorResponse(
        String code,
        String message
) {}
