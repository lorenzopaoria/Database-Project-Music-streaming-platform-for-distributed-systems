package com.example.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.logging.DatabaseAuditLogger;

public class DatabaseAuthenticator implements Authenticator {
    private final Connection connection;
    private final DatabaseAuditLogger auditLogger;

    public DatabaseAuthenticator(Connection connection) {
        this.connection = connection;
        this.auditLogger = new DatabaseAuditLogger();
    }

    @Override
    public AuthenticationResult authenticate(String email, String password) {
        try {
            String query = "SELECT tipo FROM Utente WHERE email = ? AND passw = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int tipoUtente = rs.getInt("tipo");
                        String role = tipoUtente == 0 ? "free" : "premium";
                        
                        auditLogger.logAuthentication(email, true);
                        return new AuthenticationResult(true, role, "Authentication successful");
                    }
                }
            }
            
            auditLogger.logAuthentication(email, false);
            return new AuthenticationResult(false, null, "Invalid credentials");
        } catch (SQLException e) {
            auditLogger.logAuthentication(email, false);
            return new AuthenticationResult(false, null, "Authentication error: " + e.getMessage());
        }
    }
}