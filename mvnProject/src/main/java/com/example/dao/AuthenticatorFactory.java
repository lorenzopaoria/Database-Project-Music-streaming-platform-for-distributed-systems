package com.example.security;

import java.sql.Connection;

public class AuthenticatorFactory {
    public static Authenticator createAuthenticator(String type, Connection connection) {
        switch (type.toLowerCase()) {
            case "database":
                return new DatabaseAuthenticator(connection);
            // Add more authenticator types here as needed
            default:
                throw new IllegalArgumentException("Unknown authenticator type: " + type);
        }
    }
}