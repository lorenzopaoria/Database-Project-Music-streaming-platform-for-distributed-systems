package com.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public String authenticate(String email, String password) throws SQLException {
        String query = "SELECT tipo FROM Utente WHERE email = ? AND passw = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tipoUtente = rs.getInt("tipo");
                    return tipoUtente == 0 ? "free" : "premium";
                }
            }
        }
        return null;
    }

    public String executeQuery(String query) throws SQLException {
        if (query.trim().toUpperCase().startsWith("SELECT")) {
            // Se la query è una SELECT, esegui executeQuery()
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                StringBuilder result = new StringBuilder();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String[]> rows = new ArrayList<>();
                int[] columnWidths = new int[columnCount];
                // Inizializza le larghezze con i nomi delle colonne
                for (int i = 1; i <= columnCount; i++) {
                    columnWidths[i - 1] = metaData.getColumnName(i).length();
                }
                // Leggi i dati dal ResultSet e aggiorna le larghezze
                while (rs.next()) {
                    String[] row = new String[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i) != null ? rs.getString(i) : "null";
                        row[i - 1] = value;
                        columnWidths[i - 1] = Math.max(columnWidths[i - 1], value.length());
                    }
                    rows.add(row);
                }
                // Formatta l'intestazione
                for (int i = 1; i <= columnCount; i++) {
                    result.append(String.format("%-" + columnWidths[i - 1] + "s", metaData.getColumnName(i))).append("\t");
                }
                result.append("\n");

                // Separatore
                for (int width : columnWidths) {
                    result.append("-".repeat(width)).append("\t");
                }
                result.append("\n");

                // Formatta i dati
                for (String[] row : rows) {
                    for (int i = 0; i < columnCount; i++) {
                        result.append(String.format("%-" + columnWidths[i] + "s", row[i])).append("\t");
                    }
                    result.append("\n");
                }
                return result.toString();
            }
        } else {
            // Se la query non è una SELECT, usiamo executeUpdate() (ad esempio per DROP, INSERT, UPDATE)
            try (Statement stmt = connection.createStatement()) {
                int rowsAffected = stmt.executeUpdate(query);
                return "Query eseguita con successo. " + rowsAffected + " righe interessate.";
            }
        }
    }
}
