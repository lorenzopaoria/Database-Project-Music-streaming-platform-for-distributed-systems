package com.example.dao;

import com.example.logging.DatabaseAuditLogger;
import com.example.security.Authenticator;
import com.example.security.AuthenticationResult;
import com.example.security.DatabaseAuthenticator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;
    private final DatabaseAuditLogger auditLogger;
    private final Authenticator authenticator;

    public UserDAO(Connection connection) {
        this.connection = connection;
        this.auditLogger = new DatabaseAuditLogger();
        this.authenticator = new DatabaseAuthenticator(connection);
    }

    public String authenticate(String email, String password) {
        AuthenticationResult result = authenticator.authenticate(email, password);
        return result.isSuccess() ? result.getRole() : null;
    }

    public String executeQuery(String query) throws SQLException {
        auditLogger.logQueryExecution("N/A", query, true);
        
        if (query.trim().toUpperCase().startsWith("SELECT")) {
            return executeSelectQuery(query);
        } else {
            return executeUpdateQuery(query);
        }
    }

    private String executeSelectQuery(String query) throws SQLException {
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            StringBuilder result = new StringBuilder();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Store rows and calculate column widths
            List<String[]> rows = new ArrayList<>();
            int[] columnWidths = new int[columnCount];
            
            // Initialize column widths with header lengths
            for (int i = 1; i <= columnCount; i++) {
                columnWidths[i - 1] = metaData.getColumnName(i).length();
            }
            
            // Get all rows and update column widths
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i) != null ? rs.getString(i) : "null";
                    row[i - 1] = value;
                    columnWidths[i - 1] = Math.max(columnWidths[i - 1], value.length());
                }
                rows.add(row);
            }
            
            // Build header
            for (int i = 1; i <= columnCount; i++) {
                result.append(String.format("%-" + columnWidths[i - 1] + "s", metaData.getColumnName(i))).append("\t");
            }
            result.append("\n");

            // Build separator line
            for (int width : columnWidths) {
                result.append("-".repeat(width)).append("\t");
            }
            result.append("\n");

            // Build data rows
            for (String[] row : rows) {
                for (int i = 0; i < columnCount; i++) {
                    result.append(String.format("%-" + columnWidths[i] + "s", row[i])).append("\t");
                }
                result.append("\n");
            }

            return result.toString();
        }
    }

    private String executeUpdateQuery(String query) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            int rowsAffected = stmt.executeUpdate(query);
            return "Query executed successfully. " + rowsAffected + " affected lines.";
        }
    }

    // Additional utility methods for common operations

    public boolean createUser(String email, String password, int tipo) {
        String query = "INSERT INTO Utente (email, passw, tipo) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setInt(3, tipo);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            auditLogger.logQueryExecution("N/A", "Create User: " + email, false);
            return false;
        }
    }

    public boolean updateUserType(String email, int newTipo) {
        String query = "UPDATE Utente SET tipo = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newTipo);
            stmt.setString(2, email);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            auditLogger.logQueryExecution("N/A", "Update User Type: " + email, false);
            return false;
        }
    }

    public boolean deleteUser(String email) {
        String query = "DELETE FROM Utente WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            auditLogger.logQueryExecution("N/A", "Delete User: " + email, false);
            return false;
        }
    }

    public String getUserRole(String email) {
        String query = "SELECT tipo FROM Utente WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tipo = rs.getInt("tipo");
                    return tipo == 0 ? "free" : "premium";
                }
            }
        } catch (SQLException e) {
            auditLogger.logQueryExecution("N/A", "Get User Role: " + email, false);
        }
        return null;
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // First verify old password
        if (authenticate(email, oldPassword) == null) {
            return false;
        }

        String query = "UPDATE Utente SET passw = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            auditLogger.logQueryExecution("N/A", "Change Password: " + email, false);
            return false;
        }
    }
}