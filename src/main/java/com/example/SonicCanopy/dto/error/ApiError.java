package com.example.SonicCanopy.dto.error;

public record ApiError(int status, String error, String message) {}
