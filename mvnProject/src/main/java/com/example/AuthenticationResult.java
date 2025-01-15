package com.example.security;

public class AuthenticationResult {
    private final boolean success;
    private final String role;
    private final String message;

    public AuthenticationResult(boolean success, String role, String message) {
        this.success = success;
        this.role = role;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }
}