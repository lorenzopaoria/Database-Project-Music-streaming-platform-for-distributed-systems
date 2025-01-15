package com.example.security;

public interface Authenticator {
    AuthenticationResult authenticate(String email, String password);
}

